from typing import Dict, Any, List
from models import WorkflowIntrospection, WorkflowNode, WorkflowNodeInput

class NodeIntrospector:
    def introspect(self, workflow_data: Dict[str, Any]) -> WorkflowIntrospection:
        nodes = []
        
        # Handle both API format (prompt/nodes) and UI format (workflow/nodes)
        # API format: {"3": {"inputs": {...}, "class_type": "..."}}
        # UI format: {"nodes": [...], "links": [...]} - simpler to parse API format if available
        
        # For now, let's assume we are dealing with the API format (the "prompt" style)
        # which is what we store in our .json files currently.
        
        raw_nodes = workflow_data.get("nodes", workflow_data)
        
        # Handle UI format (list of nodes)
        if isinstance(raw_nodes, list):
            for node_data in raw_nodes:
                if not isinstance(node_data, dict):
                    continue
                
                node_id = str(node_data.get("id", "unknown"))
                class_type = node_data.get("type", "Unknown") # UI format uses "type", API uses "class_type"
                label = node_data.get("title", class_type)
                
                inputs = []
                # UI format inputs are often in "widgets_values" (list) or "properties"
                # But wait, ComfyUI .json (saved from UI) structure is complex.
                # Let's look at a standard UI-saved JSON structure if possible.
                # Actually, usually "inputs" in UI format might be different.
                # Let's try to support the "widgets_values" if present, or "inputs" if present.
                
                # For now, let's try to be safe. If we can't easily parse inputs from UI format without more info,
                # we might skip deep introspection or try best effort.
                
                # However, the error was just iterating. Let's fix the iteration first.
                # If it's a list, we treat it as UI format.
                
                # In UI format, inputs might be defined in "inputs" list or "widgets_values".
                # Let's check "inputs" first (which might be a list of input slots).
                # Actually, for UI format, "widgets_values" is often where the user values are.
                
                # To keep it simple for this iteration:
                # We will try to parse "widgets_values" if available (array of values matching widgets)
                # OR "inputs" if it's a dict (unlikely in UI format).
                
                # Let's just skip detailed input introspection for UI format for now 
                # UNLESS we see a clear "inputs" dict.
                # Realistically, we need to convert UI format to API format to run it anyway.
                # But the prompt runner expects API format. 
                # If the user has UI-format JSONs, sending them to /prompt might fail if ComfyUI doesn't convert them.
                # ComfyUI's /prompt endpoint expects API format.
                # So if we load a UI-format JSON, we might need to convert it or it won't work at all.
                
                # For the purpose of *introspection*, let's just fix the crash.
                pass

        # Handle API format (dict of nodes)
        elif isinstance(raw_nodes, dict):
            for node_id, node_data in raw_nodes.items():
                if not isinstance(node_data, dict):
                    continue
                    
                class_type = node_data.get("class_type", "Unknown")
                meta = node_data.get("_meta", {})
                label = meta.get("title", class_type)
                
                inputs = []
                raw_inputs = node_data.get("inputs", {})
                
                for input_name, input_value in raw_inputs.items():
                    # We only care about primitive inputs (int, float, str, bool)
                    # We ignore lists (links to other nodes)
                    
                    if isinstance(input_value, list):
                        continue
                    
                    input_type = "string"
                    if isinstance(input_value, bool):
                        input_type = "bool"
                    elif isinstance(input_value, int):
                        input_type = "int"
                    elif isinstance(input_value, float):
                        input_type = "float"
                    
                    # More specific seed detection - only match fields that are actually seeds
                    # Common seed field names: seed, noise_seed, rand_seed, random_seed
                    input_lower = input_name.lower()
                    is_seed = (
                        input_lower == "seed" or 
                        input_lower == "noise_seed" or 
                        input_lower == "rand_seed" or
                        input_lower == "random_seed" or
                        input_lower.endswith("_seed")
                    )
                    
                    inputs.append(WorkflowNodeInput(
                        name=input_name,
                        type=input_type,
                        default=input_value,
                        is_seed=is_seed
                    ))
                
                if inputs:
                    nodes.append(WorkflowNode(
                        id=node_id,
                        type=class_type,
                        label=label,
                        inputs=inputs
                    ))
                
        return WorkflowIntrospection(nodes=nodes)

node_introspector = NodeIntrospector()
