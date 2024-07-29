from fastapi import FastAPI
import os
import sys

sys.path.append(os.path.join(os.path.dirname(os.path.abspath(os.path.dirname(__file__))), 'yolov9'))

from router import *

app = FastAPI()

#---------------------------------------------------------------------
app.include_router(debug.router)
app.include_router(board.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)