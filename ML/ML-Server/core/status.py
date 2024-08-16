
# main.app.game
# game은 어플리케이션과 라이프사이클을 함께한다
# board.py에서 호출

class GameStatus:
    def __init__(self):
        print('GameStatus Constructor')
        self.initializing()

    # game 초기화
    def initializing(self):
        self.running = False
        self.playerNum = 0
        self.deckCard = None
        self.playersCard = None
        print("game_initializing")

    # game 시작
    def game_start(self, playerNum):
        self.running = True
        self.playerNum = playerNum
        print("game_start")

    # game 종료
    def game_end(self):
        self.running = False
        print("game_end")