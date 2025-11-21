from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .routers import workflows, jobs

# Initialize FastAPI app
app = FastAPI(
    title="ComfyUI Wrapper API",
    description="A simplified REST interface for ComfyUI",
    version="0.1.0"
)

# Configure CORS
# TODO: Adjust allow_origins for production security
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],  # Allows all methods
    allow_headers=["*"],  # Allows all headers
)

# Include routers
app.include_router(workflows.router)
app.include_router(jobs.router)

@app.get("/")
async def root():
    """
    Root endpoint to verify API is running.
    """
    return {"message": "ComfyUI Wrapper API is running"}

# TODO: Add authentication hook here
# def get_current_user(): ...
