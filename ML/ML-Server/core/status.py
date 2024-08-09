class GameStatus:
    def __init__(self):
        print('GameStatus Constructor')
        self.initializing()

    def initializing(self):
        self.running = False
        self.playerNum = 0
        self.deckCard = None
        self.playersCard = None

    def game_start(self, playerNum):
        self.running = True
        self.playerNum = playerNum

    def ambassador_action(self, playerId):
        pass

    def ambassador_action_done(self):
        pass

    def card_realloc(self):
        pass

    def game_end(self):
        self.running = False