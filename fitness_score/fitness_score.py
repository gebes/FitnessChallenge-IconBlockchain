from iconservice import *

TAG = 'FitnessScore'

# * Important console commands for testnet
# $ docker container start -a local-tbears
# $ docker container attach local-tbears
# $ tbears balance [account] -u https://bicon.net.solidwallet.io/api/v3
# $ tbears deploy fitness_score/ -k wallets/test1_Account -u https://bicon.net.solidwallet.io/api/v3


class FitnessScore(IconScoreBase):

    def __init__(self, db: IconScoreDatabase) -> None:
        super().__init__(db)

        self._myChallenger = DictDB('myChallengers', db, value_type=Address)
        self._myChallengerBet = DictDB('myChallengerBet', db, value_type=str)

    def on_install(self) -> None:
        super().on_install()

    def on_update(self) -> None:
        super().on_update()

    @external(readonly=True)
    def hello(self) -> str:
        Logger.debug(f'Hello, world!', TAG)
        return "Hello"

    @payable
    def fallback(self):
        if self.msg.value > 0:
            self.Deposit(self.msg.sender, self.msg.value)

    # Returns your current challenger (if there exists one)
    @external(readonly=True)
    def get_my_challengers(self) -> str:

        challenger = self._myChallenger[self.msg.sender]

        if challenger == None:
            return str(None)

        bet = self._myChallengerBet[self.msg.sender]

        return str(bet) + ":" + str(challenger)
    
    @external
    @payable
    def challenge(self) -> str:
        
        revert("hahahahah")


