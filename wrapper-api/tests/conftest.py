import pytest
from fastapi.testclient import TestClient
import respx
from httpx import Response
from main import app
from config import settings

@pytest.fixture
def client():
    return TestClient(app)

@pytest.fixture
def mock_comfy():
    with respx.mock(base_url=settings.comfyui_base_url) as respx_mock:
        yield respx_mock
