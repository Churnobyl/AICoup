from service.players_service import tracePlayers
from test.inference_data import inferResult 

def testPlayers():
  print('player test')
    
  print(tracePlayers(inferResult))