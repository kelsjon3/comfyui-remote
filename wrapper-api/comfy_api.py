import httpx
import uuid
import json
import random
from typing import Dict, Any, Optional
from config import config
from models import RunWorkflowRequest, JobResponse

class ComfyAPI:
    def __init__(self):
        self.client_id = str(uuid.uuid4())

    async def _request(self, method: str, endpoint: str, json_data: Any = None):
        url = f"{config.COMFYUI_URL}{endpoint}"
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
                raise Exception(f"ComfyUI unreachable: {exc}")
            except httpx.HTTPStatusError as exc:
                raise Exception(f"ComfyUI error: {exc.response.text}")

    async def queue_prompt(self, workflow: Dict[str, Any]) -> str:
        payload = {
            "prompt": workflow,
            "client_id": self.client_id
        }
        response = await self._request("POST", "/prompt", payload)
        return response.json().get("prompt_id")

    async def get_history(self, prompt_id: str) -> Optional[Dict[str, Any]]:
        response = await self._request("GET", f"/history/{prompt_id}")
        history = response.json()
        return history.get(prompt_id)

    async def get_queue(self):
        response = await self._request("GET", "/queue")
        return response.json()

    async def get_image(self, filename: str, subfolder: str = "", type: str = "output") -> bytes:
        params = f"?filename={filename}&subfolder={subfolder}&type={type}"
        response = await self._request("GET", f"/view{params}")
        return response.content

comfy_api = ComfyAPI()
