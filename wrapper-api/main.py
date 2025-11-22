import asyncio
import base64
from typing import List, Dict, Any
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware

from config import config
from models import (
    WorkflowSummary, 
    WorkflowIntrospection, 
    RunWorkflowRequest, 
    JobResponse
)
from workflow_loader import workflow_loader
from node_introspection import node_introspector
from job_history import job_history
from comfy_api import comfy_api

app = FastAPI(title="ComfyUI Remote Wrapper")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/workflows", response_model=List[WorkflowSummary])
async def list_workflows():
    return workflow_loader.list_workflows()

@app.get("/workflow/{name}", response_model=Dict[str, Any])
async def get_workflow(name: str):
    workflow = workflow_loader.load_workflow(name)
    if not workflow:
        raise HTTPException(status_code=404, detail="Workflow not found")
    return workflow

@app.get("/workflow/{name}/introspect", response_model=WorkflowIntrospection)
async def introspect_workflow(name: str):
    workflow = workflow_loader.load_workflow(name)
    if not workflow:
        raise HTTPException(status_code=404, detail="Workflow not found")
    return node_introspector.introspect(workflow)

@app.post("/run", response_model=JobResponse)
async def run_workflow(request: RunWorkflowRequest, background_tasks: BackgroundTasks):
    # 1. Load Workflow
    workflow_data = workflow_loader.load_workflow(request.workflow_name)
    if not workflow_data:
        raise HTTPException(status_code=404, detail="Workflow not found")
    
    # 2. Check workflow format
    nodes = workflow_data.get("nodes", workflow_data)
    
    # If nodes is a list (UI format), we can't easily modify it
    # ComfyUI's /prompt endpoint expects API format (dict of nodes)
    if isinstance(nodes, list):
        raise HTTPException(
            status_code=400, 
            detail="Workflow is in UI format. Please export as API format from ComfyUI (Save (API Format) option)."
        )
    
    # 3. Prepare Workflow (Apply Inputs)
    resolved_inputs = {}
    
    for key, value in request.inputs.items():
        # key format: "node_id.input_name"
        try:
            node_id, input_name = key.split(".", 1)
            if node_id in nodes and "inputs" in nodes[node_id]:
                nodes[node_id]["inputs"][input_name] = value
                resolved_inputs[key] = value
        except ValueError:
            continue

    # 4. Handle Seed
    resolved_seed = 0
    if request.seed_control.mode == "random":
        import random
        resolved_seed = random.randint(1, 100000000000000)
    elif request.seed_control.value is not None:
        resolved_seed = request.seed_control.value

    # Apply seed to all seed inputs
    introspection = node_introspector.introspect(workflow_data)
    for node in introspection.nodes:
        for inp in node.inputs:
            if inp.is_seed:
                if node.id in nodes and "inputs" in nodes[node.id]:
                    nodes[node.id]["inputs"][inp.name] = resolved_seed

    # 5. Submit to ComfyUI
    try:
        prompt_id = await comfy_api.queue_prompt(workflow_data)
    except Exception as e:
        import traceback
        print(f"Error submitting to ComfyUI: {e}")
        print(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"ComfyUI error: {str(e)}")

    # 6. Create Job Response
    job = JobResponse(
        job_id=prompt_id,
        workflow_name=request.workflow_name,
        status="queued",
        resolved_inputs=resolved_inputs,
        resolved_seed=resolved_seed
    )
    job_history.add_job(job)

    # 7. Start Background Polling
    background_tasks.add_task(poll_job, prompt_id)

    return job

async def poll_job(prompt_id: str):
    """Polls ComfyUI for job completion and updates history."""
    import asyncio
    
    while True:
        try:
            history = await comfy_api.get_history(prompt_id)
            if history:
                # Job finished
                outputs = history.get("outputs", {})
                image_url = None
                
                # Find first image
                for node_id, output in outputs.items():
                    if "images" in output:
                        for img in output["images"]:
                            filename = img.get("filename")
                            subfolder = img.get("subfolder", "")
                            type_ = img.get("type", "output")
                            
                            # Construct a proxy URL for the frontend
                            # We need a new endpoint to serve these images via proxy
                            image_url = f"/proxy/image?filename={filename}&subfolder={subfolder}&type={type_}"
                            break
                    if image_url: break
                
                job_history.update_job_status(prompt_id, "completed", image_url)
                break
            
            # Check if failed? (ComfyUI doesn't make this easy via history, usually need to check queue)
            # For now, simple polling
            await asyncio.sleep(1)
        except Exception as e:
            print(f"Polling error for {prompt_id}: {e}")
            break

@app.get("/history", response_model=List[JobResponse])
async def get_history():
    return job_history.list_jobs()

@app.get("/proxy/image")
async def proxy_image(filename: str, subfolder: str = "", type: str = "output"):
    from fastapi.responses import Response
    try:
        content = await comfy_api.get_image(filename, subfolder, type)
        return Response(content=content, media_type="image/png") # Assume PNG for now
    except Exception as e:
        raise HTTPException(status_code=404, detail=str(e))

@app.get("/checkpoints")
async def get_checkpoints():
    """Get list of available checkpoints from ComfyUI."""
    try:
        object_info = await comfy_api.get_object_info()
        
        # Extract checkpoint loaders
        checkpoints = []
        for node_type, node_info in object_info.items():
            if "CheckpointLoader" in node_type or "checkpoint" in node_type.lower():
                # Get the checkpoint input configuration
                if "input" in node_info and "required" in node_info["input"]:
                    for input_name, input_config in node_info["input"]["required"].items():
                        if input_name == "ckpt_name" or "checkpoint" in input_name.lower():
                            if isinstance(input_config, list) and len(input_config) > 0:
                                # First element is usually the list of available options
                                if isinstance(input_config[0], list):
                                    checkpoints.extend(input_config[0])
        
        # Remove duplicates and sort
        checkpoints = sorted(list(set(checkpoints)))
        return {"checkpoints": checkpoints}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get checkpoints: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
