class GameStatus:
    def __init__(self):
        self.running = False
        self.firstInfer = False
        self.deckCard = None
        self.playersCard = None
        self.action = None

    def initializing(self, playerNum):
        self.running = True
        self.firstInfer = False
        self.playerNum = playerNum
        self.deckCard = None
        self.playersCard = None

    def start(self):
        pass