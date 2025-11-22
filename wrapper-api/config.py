import os
import argparse
import yaml
from pathlib import Path
from typing import Optional

# Defaults
DEFAULT_COMFYUI_URL = "http://localhost:8188"
CONFIG_FILE_NAME = "config.yaml"
DEFAULT_WORKFLOW_PATHS = [
    "~/ComfyUI/user/default/workflows",
    "~/comfyui/user/default/workflows",
    "~/.pinokio/apps/comfyui/app/user/default/workflows",
    "~/pinokio/api/comfy.git/app/user/default/workflows",
]

class Config:
    def __init__(self):
        self.WORKFLOW_DIR: Optional[Path] = None
        self.COMFYUI_URL: str = DEFAULT_COMFYUI_URL
        self._load_config()

    def _load_config(self):
        # 1. Parse CLI args (preliminary, just to check for overrides)
        parser = argparse.ArgumentParser(description="ComfyUI Remote Wrapper Backend")
        parser.add_argument("--workflow-dir", type=str, help="Path to ComfyUI workflows directory")
        parser.add_argument("--comfyui-url", type=str, help="URL of the ComfyUI instance")
        args, _ = parser.parse_known_args()

        # 2. Check Environment Variables
        env_workflow_dir = os.environ.get("COMFYUI_WORKFLOW_DIR")
        env_comfyui_url = os.environ.get("COMFYUI_URL")

        # 3. Check Config File
        config_file_path = Path.home() / ".comfyui-remote" / CONFIG_FILE_NAME
        local_config_path = Path.cwd() / CONFIG_FILE_NAME
        
        file_config = {}
        if config_file_path.exists():
            with open(config_file_path, "r") as f:
                file_config = yaml.safe_load(f) or {}
        elif local_config_path.exists():
             with open(local_config_path, "r") as f:
                file_config = yaml.safe_load(f) or {}

        # Resolve COMFYUI_URL
        if args.comfyui_url:
            self.COMFYUI_URL = args.comfyui_url
        elif env_comfyui_url:
            self.COMFYUI_URL = env_comfyui_url
        elif "comfyui_url" in file_config:
            self.COMFYUI_URL = file_config["comfyui_url"]
        
        # Resolve WORKFLOW_DIR
        workflow_dir_str = None
        
        if args.workflow_dir:
            workflow_dir_str = args.workflow_dir
        elif env_workflow_dir:
            workflow_dir_str = env_workflow_dir
        elif "workflow_dir" in file_config:
            workflow_dir_str = file_config["workflow_dir"]
            
        if workflow_dir_str:
            path = Path(workflow_dir_str).expanduser().resolve()
            if path.exists() and path.is_dir():
                self.WORKFLOW_DIR = path
        
        # 4. Auto-detection
        if self.WORKFLOW_DIR is None:
            for path_str in DEFAULT_WORKFLOW_PATHS:
                path = Path(path_str).expanduser().resolve()
                if path.exists() and path.is_dir():
                    self.WORKFLOW_DIR = path
                    print(f"Auto-detected workflow directory: {self.WORKFLOW_DIR}")
                    break
        
        if self.WORKFLOW_DIR is None:
            print("WARNING: Could not find ComfyUI workflows directory. Please configure it via CLI, Env, or Config file.")

# Global config instance
config = Config()
