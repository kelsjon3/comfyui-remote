import json
import os
from pathlib import Path
from typing import List, Optional
from datetime import datetime
from config import config
from models import WorkflowSummary

class WorkflowLoader:
    def __init__(self):
        pass

    def _get_workflow_dir(self) -> Path:
        if not config.WORKFLOW_DIR:
             raise FileNotFoundError("Workflow directory not configured")
        return config.WORKFLOW_DIR

    def list_workflows(self) -> List[WorkflowSummary]:
        workflow_dir = self._get_workflow_dir()
        if not workflow_dir.exists():
            return []

        workflows = []
        for file_path in workflow_dir.glob("*.json"):
            stat = file_path.stat()
            workflows.append(WorkflowSummary(
                file_name=file_path.name,
                name=file_path.stem,
                last_modified=datetime.fromtimestamp(stat.st_mtime)
            ))
        return workflows

    def load_workflow(self, name: str) -> Optional[dict]:
        workflow_dir = self._get_workflow_dir()
        
        # Strip .json extension if present
        if name.endswith('.json'):
            name = name[:-5]
        
        file_path = workflow_dir / f"{name}.json"
        
        if not file_path.exists():
            return None
            
        try:
            with open(file_path, "r") as f:
                return json.load(f)
        except Exception as e:
            print(f"Error loading workflow {name}: {e}")
            return None

workflow_loader = WorkflowLoader()
