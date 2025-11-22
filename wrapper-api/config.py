from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    app_name: str = "ComfyUI Wrapper API"
    comfyui_base_url: str = "http://localhost:8188"
    workflow_dir: str = "./workflows"

    model_config = SettingsConfigDict(env_file=".env")

settings = Settings()
