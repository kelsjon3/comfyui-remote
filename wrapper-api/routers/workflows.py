from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any

router = APIRouter(
    prefix="/workflows",
    tags=["workflows"]
)

# Placeholder for workflow directory path
# TODO: Load this from configuration
WORKFLOW_DIR = "./workflows"

@router.get("/", response_model=List[Dict[str, Any]])
async def list_workflows():
    """
    List available workflows.
    
    Returns:
        List of workflow summaries (id, name, description).
    """
    # TODO: Scan WORKFLOW_DIR and return list of .json files
    return [
        {"id": "workflow_1", "name": "Text to Image", "description": "Basic text to image generation"},
        {"id": "workflow_2", "name": "Image to Image", "description": "Modify existing image"}
    ]

@router.get("/{id}", response_model=Dict[str, Any])
async def get_workflow(id: str):
    """
    Retrieve workflow JSON + parsed nodes.
    
    Args:
        id: The ID of the workflow to retrieve.
        
    Returns:
        Full workflow JSON object.
    """
    # TODO: Read specific workflow file from WORKFLOW_DIR
    if id == "workflow_1":
        return {
            "id": "workflow_1",
            "nodes": [
                {"id": 1, "type": "KSampler", "inputs": {}},
                {"id": 2, "type": "CheckpointLoaderSimple", "inputs": {}}
            ]
        }
    raise HTTPException(status_code=404, detail="Workflow not found")
