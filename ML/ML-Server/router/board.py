from fastapi import APIRouter
from fastapi.responses import JSONResponse

from service.capture import capture_photos
from service.model import inference
from util_func import makeDetectionsJsonResult

router = APIRouter(
    prefix='/api/board'
)

# @router.get('')
# async def getBoardInfo():
#     results = inference()
#     return JSONResponse(content={"results": results})

# @router.post('')
# async def postBoardInfo():
#     capture_photos()

@router.get('/images')
async def getImages():
    pass

@router.post('/images')
async def postImages():
    capture_photos()
    results = inference()
    return JSONResponse(content={"results": results})

@router.get('/labels')
async def getLabels():
    results = makeDetectionsJsonResult(inference())
    return JSONResponse(content={"results": results})

@router.post('/labels')
async def postLabels():
    capture_photos()
    results = makeDetectionsJsonResult(inference())
    return JSONResponse(content={"results": results})

@router.get('/game-status')
async def getGameStatus():
    results = makeDetectionsJsonResult(inference())
    return JSONResponse(content={"results": results})

@router.post('/game-status')
async def postGameStatus():
    capture_photos()
    results = makeDetectionsJsonResult(inference())
    return JSONResponse(content={"results": results})