import pytest
import json
from httpx import Response

def test_start_job(client, mock_comfy):
    # Mock ComfyUI /prompt endpoint
    mock_comfy.post("/prompt").mock(return_value=Response(200, json={"prompt_id": "job_123"}))
    
    response = client.post("/jobs/start?workflow_id=basic_txt2img.json")
    assert response.status_code == 200
    data = response.json()
    assert data["job_id"] == "job_123"
    assert data["status"] == "queued"

def test_stop_job(client, mock_comfy):
    # Mock ComfyUI /interrupt endpoint
    mock_comfy.post("/interrupt").mock(return_value=Response(200))
    
    response = client.post("/jobs/job_123/stop")
    assert response.status_code == 200
    assert response.json()["status"] == "stopped"

def test_get_job_status_completed(client, mock_comfy):
    # Mock ComfyUI /history/{id} endpoint
    history_data = {
        "job_123": {
            "outputs": {},
            "status": {"status_str": "success"}
        }
    }
    mock_comfy.get("/history/job_123").mock(return_value=Response(200, json=history_data))
    
    response = client.get("/jobs/job_123")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "completed"

def test_get_job_status_running(client, mock_comfy):
    # Mock ComfyUI /history/{id} (not found)
    mock_comfy.get("/history/job_123").mock(return_value=Response(200, json={}))
    
    # Mock ComfyUI /queue endpoint
    queue_data = {
        "queue_running": [["123", "job_123", "workflow", "client_id", "extra"]],
        "queue_pending": []
    }
    mock_comfy.get("/queue").mock(return_value=Response(200, json=queue_data))
    
    response = client.get("/jobs/job_123")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "queue_running"

def test_list_job_images(client, mock_comfy):
    # Mock ComfyUI /history/{id} endpoint
    history_data = {
        "job_123": {
            "outputs": {
                "9": {
                    "images": [
                        {"filename": "image_1.png", "subfolder": "", "type": "output"}
                    ]
                }
            }
        }
    }
    mock_comfy.get("/history/job_123").mock(return_value=Response(200, json=history_data))
    
    response = client.get("/jobs/job_123/images")
    assert response.status_code == 200
    data = response.json()
    assert "image_1.png" in data
