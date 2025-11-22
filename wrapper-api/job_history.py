from typing import List, Optional, Dict
from datetime import datetime
from models import JobResponse

class JobHistory:
    def __init__(self, max_size: int = 50):
        self.max_size = max_size
        self._jobs: List[JobResponse] = []
        self._jobs_map: Dict[str, JobResponse] = {}

    def add_job(self, job: JobResponse):
        self._jobs.insert(0, job)
        self._jobs_map[job.job_id] = job
        
        if len(self._jobs) > self.max_size:
            removed = self._jobs.pop()
            if removed.job_id in self._jobs_map:
                del self._jobs_map[removed.job_id]

    def get_job(self, job_id: str) -> Optional[JobResponse]:
        return self._jobs_map.get(job_id)

    def list_jobs(self, limit: int = 20) -> List[JobResponse]:
        return self._jobs[:limit]

    def update_job_status(self, job_id: str, status: str, image_url: Optional[str] = None):
        job = self.get_job(job_id)
        if job:
            job.status = status
            if image_url:
                job.image_url = image_url

job_history = JobHistory()
