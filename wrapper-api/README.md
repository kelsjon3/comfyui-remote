# ComfyUI Wrapper API

A simplified REST interface for ComfyUI, designed to be consumed by mobile applications.

## Architecture

This service sits between the Android app and the ComfyUI instance. It handles:
- Workflow management
- Job submission and control
- Status polling
- Image retrieval

## Setup

### Local Development

1. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
2. Run the server:
   ```bash
   uvicorn main:app --reload
   ```

### Docker

1. Build and run:
   ```bash
   docker-compose up --build
   ```

## API Endpoints

### Workflows
- `GET /workflows`: List available workflows
- `GET /workflows/{id}`: Get workflow details

### Jobs
- `POST /jobs/start`: Start a new job
- `POST /jobs/{job_id}/stop`: Stop a running job
- `GET /jobs/{job_id}`: Get job status
- `GET /jobs/{job_id}/images`: List job images
- `GET /jobs/{job_id}/images/{index}`: Get specific image

## Configuration

TODO: Add configuration details for:
- ComfyUI Base URL
- Workflow Directory
- Authentication
