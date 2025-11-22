import pytest
import os
import json

def test_list_workflows(client):
    response = client.get("/workflows/")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    # We expect at least the basic_txt2img.json we created
    assert any(w["id"] == "basic_txt2img.json" for w in data)

def test_get_workflow(client):
    response = client.get("/workflows/basic_txt2img.json")
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Basic Text to Image"
    assert "nodes" in data

def test_get_workflow_not_found(client):
    response = client.get("/workflows/non_existent.json")
    assert response.status_code == 404
