from pydantic import BaseModel
from typing import List, Dict, Any, Optional
from datetime import datetime

class WorkflowSummary(BaseModel):
    file_name: str
    name: str
    last_modified: datetime

class WorkflowNodeInput(BaseModel):
    name: str
    type: str
    default: Any = None
    is_seed: bool = False

class WorkflowNode(BaseModel):
    id: str
    type: str
    label: Optional[str] = None
    inputs: List[WorkflowNodeInput]

class WorkflowIntrospection(BaseModel):
    nodes: List[WorkflowNode]

class SeedControl(BaseModel):
    mode: str = "random" # fixed, random
    value: Optional[int] = None

class RunWorkflowRequest(BaseModel):
    workflow_name: str
    inputs: Dict[str, Any] = {}
    seed_control: SeedControl = SeedControl()

class JobResponse(BaseModel):
    job_id: str
    workflow_name: str
    status: str
    resolved_inputs: Dict[str, Any]
    resolved_seed: int
    image_url: Optional[str] = None
