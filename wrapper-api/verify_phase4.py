import sys
import os
import json

# Add current directory to sys.path
sys.path.append(os.getcwd())

from config import config
from workflow_loader import workflow_loader
from node_introspection import node_introspector

def verify():
    print(f"Configured Workflow Dir: {config.WORKFLOW_DIR}")
    print(f"ComfyUI URL: {config.COMFYUI_URL}")
    
    workflows = workflow_loader.list_workflows()
    print(f"Found {len(workflows)} workflows:")
    for wf in workflows:
        print(f" - {wf.name} ({wf.file_name})")
        
        # Try loading and introspecting
        data = workflow_loader.load_workflow(wf.name)
        if data:
            introspection = node_introspector.introspect(data)
            print(f"   -> Introspected {len(introspection.nodes)} nodes with configurable inputs.")
            for node in introspection.nodes:
                if any(i.is_seed for i in node.inputs):
                    print(f"      - Node {node.id} ({node.label}) has seed input.")

if __name__ == "__main__":
    verify()
