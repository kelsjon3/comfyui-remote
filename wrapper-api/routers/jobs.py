from fastapi import APIRouter, HTTPException, Response
from typing import Dict, Any, List
import httpx
import uuid
import json
import random
from config import settings

router = APIRouter(
    prefix="/jobs",
    tags=["jobs"]
)

async def comfy_request(method: str, endpoint: str, json_data: Any = None):
    """Helper to make requests to ComfyUI"""
    url = f"{settings.comfyui_base_url}{endpoint}"
    async with httpx.AsyncClient() as client:
        try:
            if method == "GET":
                response = await client.get(url)
            elif method == "POST":
                response = await client.post(url, json=json_data)
            else:
                raise ValueError(f"Unsupported method: {method}")
            
            response.raise_for_status()
            return response
        except httpx.RequestError as exc:
            raise HTTPException(status_code=503, detail=f"ComfyUI unreachable: {exc}")
        except httpx.HTTPStatusError as exc:
            raise HTTPException(status_code=exc.response.status_code, detail=f"ComfyUI error: {exc.response.text}")

@router.post("/start", response_model=Dict[str, Any])
async def start_job(workflow_id: str, node_updates: Dict[str, Any] = None):
    """
    Start a job (via ComfyUI /prompt).
    """
    # 1. Load workflow JSON
    # We reuse the logic from workflows router or just read file directly
    # Ideally we should have a service layer, but for now direct file read is fine
    import os
    workflow_path = os.path.join(settings.workflow_dir, workflow_id)
    if not os.path.exists(workflow_path):
        raise HTTPException(status_code=404, detail="Workflow not found")
    
    with open(workflow_path, 'r') as f:
        workflow_data = json.load(f)
    
    workflow = workflow_data.get("nodes", workflow_data) # Handle both wrapped and raw formats

    # 2. Apply node_updates
    if node_updates:
        for node_id, updates in node_updates.items():
            if node_id in workflow:
                if "inputs" in workflow[node_id]:
                    workflow[node_id]["inputs"].update(updates)

    # 3. Post to ComfyUI /prompt endpoint
    # Randomize seed to ensure new generation
    # TODO: Make this more robust by finding the KSampler node dynamically
    # For now, we know node "3" is KSampler in our basic workflow
    if "3" in workflow and "inputs" in workflow["3"] and "seed" in workflow["3"]["inputs"]:
        workflow["3"]["inputs"]["seed"] = random.randint(1, 100000000000000)

    payload = {
        "prompt": workflow,
        "client_id": "comfyui_remote_wrapper" # Track our client
    }
    response = await comfy_request("POST", "/prompt", payload)
    data = response.json()
    
    return {"job_id": data.get("prompt_id"), "status": "queued"}

@router.post("/{job_id}/stop", response_model=Dict[str, Any])
async def stop_job(job_id: str):
    """
    Stop a job (via ComfyUI /interrupt).
    """
    await comfy_request("POST", "/interrupt")
    return {"job_id": job_id, "status": "stopped"}

@router.get("/{job_id}", response_model=Dict[str, Any])
async def get_job_status(job_id: str):
    """
    Check job status.
    """
    # Check history first (finished jobs)
    history_resp = await comfy_request("GET", f"/history/{job_id}")
    history = history_resp.json()
    
    if job_id in history:
        return {"job_id": job_id, "status": "completed", "details": history[job_id]}
        
    # Check queue (pending/running)
    queue_resp = await comfy_request("GET", "/queue")
    queue = queue_resp.json()
    
    for status in ["queue_running", "queue_pending"]:
        for item in queue.get(status, []):
            if item[1] == job_id:
                return {"job_id": job_id, "status": status}
                
    return {"job_id": job_id, "status": "unknown"}

@router.get("/{job_id}/images", response_model=List[str])
async def list_job_images(job_id: str):
    """
    List images from finished jobs.
    """
    history_resp = await comfy_request("GET", f"/history/{job_id}")
    history = history_resp.json()
    
    if job_id not in history:
        raise HTTPException(status_code=404, detail="Job not found or not finished")
        
    outputs = history[job_id].get("outputs", {})
    images = []
    for node_id, node_output in outputs.items():
        if "images" in node_output:
            for img in node_output["images"]:
                images.append(img.get("filename"))
                
    return images

@router.get("/{job_id}/images/{filename}")
async def get_job_image(job_id: str, filename: str):
    """
    Retrieve a specific image from a job.
    """
    # We need to find the image details (subfolder, type) from history again
    # Or we can just try to fetch it from ComfyUI /view
    
    # Simple proxy to /view
    params = f"?filename={filename}"
    url = f"{settings.comfyui_base_url}/view{params}"
    
    async with httpx.AsyncClient() as client:
        response = await client.get(url)
        if response.status_code != 200:
             raise HTTPException(status_code=response.status_code, detail="Image not found")
        
        return Response(content=response.content, media_type=response.headers.get("content-type"))
