from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any
import os
import json
from config import settings

router = APIRouter(
    prefix="/workflows",
    tags=["workflows"]
)

@router.get("/", response_model=List[Dict[str, Any]])
async def list_workflows():
    """
    List available workflows.
    
    Returns:
        List of workflow summaries (id, name, description).
    """
    workflows = []
    workflow_dir = settings.workflow_dir
    
    if not os.path.exists(workflow_dir):
        return []

    for filename in os.listdir(workflow_dir):
        if filename.endswith(".json"):
            file_path = os.path.join(workflow_dir, filename)
            try:
                with open(file_path, 'r') as f:
                    data = json.load(f)
                    # Attempt to extract metadata, fallback to filename
                    name = data.get("name", filename.replace(".json", ""))
                    description = data.get("description", "No description provided")
                    workflows.append({
                        "id": filename,
                        "name": name,
                        "description": description
                    })
            except json.JSONDecodeError:
                continue # Skip invalid JSON files
                
    return workflows

@router.get("/{id}", response_model=Dict[str, Any])
async def get_workflow(id: str):
    """
    Retrieve workflow JSON + parsed nodes.
    
    Args:
        id: The ID of the workflow to retrieve (filename).
        
    Returns:
        Full workflow JSON object.
    """
    workflow_dir = settings.workflow_dir
    file_path = os.path.join(workflow_dir, id)
    
    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="Workflow not found")
        
    try:
        with open(file_path, 'r') as f:
            return json.load(f)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error reading workflow: {str(e)}")
