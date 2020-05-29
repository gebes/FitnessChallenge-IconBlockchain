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

        # Point counter during the challenge
        self._myPoints = DictDB('myPoints', db, value_type=int)
        # Person you challenged
        self._myTarget = DictDB('myTarget', db, value_type=Address)
        # Person which challenged you
        self._myChallenger = DictDB('myChallengers', db, value_type=Address)
        self._myChallengerStartTime = DictDB('myChallengerStartTime', db, value_type=int)
        self._myChallengerDuration = DictDB('myChallengerDuration', db, value_type=int)
        self._myChallengersBet = DictDB('myChallengersBet', db, value_type=int)
        self._myChallengerAccepted = DictDB('myChallengerAccepted', db, value_type=bool)

        # Result of the last challenge
        self._lastChallengeResult = DictDB('lastChallengeResult', db, value_type=str)

    def on_install(self) -> None:
        super().on_install()

    def on_update(self) -> None:
        super().on_update()

    @payable
    def fallback(self):
        if self.msg.value > 0:
            self.icx.send(self.msg.sender, self.msg.value)

    # Returns your current challenger (if there exists one)
    @external(readonly=True)
    def get_my_challengers(self) -> str:

        challenger = self._myChallenger[self.msg.sender]

        if challenger is None:
            return "None"

        bet = self._myChallengersBet[self.msg.sender]

        return str(bet) + ":" + str(challenger) + ":" + str(self._myChallengerStartTime[self.msg.sender]) + ":" + str(self._myChallengerDuration[self.msg.sender])

    @external
    @payable
    def check_if_challenge_ended(self):

        requester = self.msg.sender

        if self._myTarget[requester] is not None:
            target = self._myTarget[requester]

            if self.has_challenge_ended(self._myChallengerStartTime[target], self._myChallengerDuration[target]):
                self.clear_my_challenger_with_payout(target)
        elif self._myChallenger[requester] is not None:

            target = requester

            if self.has_challenge_ended(self._myChallengerStartTime[target], self._myChallengerDuration[target]):
                self.clear_my_challenger_with_payout(target)

    @external(readonly=True)
    def last_challenge_result(self) -> str:

        result: str = self._lastChallengeResult[self.msg.sender]

        if result is None or not result:
            return "You don't have any last results."

        return result

    @external(readonly=True)
    def has_my_target_accepted(self) -> str:
        my_target = self._myTarget[self.msg.sender]

        if my_target is None or self._myChallengerStartTime[my_target] + self._myChallengerDuration[my_target] <= self.current_time_seconds():
            return "None"

        return str(self._myChallengerAccepted[my_target]).lower() + ":" + str(my_target) + ":" + str(self._myChallengerStartTime[my_target]) + ":" + str(self._myChallengerDuration[my_target])

    @external
    @payable
    def submit_points(self, points: int):

        requester = self.msg.sender

        if self._myTarget[requester] is not None:
            # when the submitter is the person who created the challenge...

            target = self._myTarget[requester]
            self._add_points(target, requester, points)

        elif self._myChallenger[requester] is not None:

            target = requester
            self._add_points(target, requester, points)
        else:
            self.refund("You are currently in no challenge.")

    def _add_points(self, target: Address, requester: Address, points: int):
        startTime = self._myChallengerStartTime[target]
        duration = self._myChallengerDuration[target]

        if not self.has_challenge_started(startTime):
            self.refund("The challenge hasn't started yet.")

        if self.has_challenge_ended(startTime, duration):
            self.clear_my_challenger_with_payout(target)
            self.refund("The challenge has ended.")

        self._myPoints[requester] += points

    @external
    @payable
    def accept(self):
        if self._myChallengerAccepted[self.msg.sender]:
            self.refund("You already have accepted this challenge.")

        if self._myChallenger[self.msg.sender] is None:
            self.refund("You do not have any challenger.")

        if self._myChallengerStartTime[self.msg.sender] <= self.current_time_seconds_tx():
            self.clear_my_challenger_with_payout(self.msg.sender)
            self.refund("You haven't accepted fast enough. The challenge already should have began.")

        if str(self.msg.value) != str(self._myChallengersBet[self.msg.sender]):
            self.refund("You haven't transferred the bet of " + str(self._myChallengersBet[self.msg.sender]) + ".")

        self._myChallengerAccepted[self.msg.sender] = True

    @external
    @payable
    def deny(self):
        if self._myChallengerAccepted[self.msg.sender]:
            self.refund("You have already accept this challenge. Don't be a coward.")

        self.clear_my_challenger_with_payout(self.msg.sender)

    @external
    @payable
    def challenge(self, target: Address, startTime: int, duration: int):
        if str(self.msg.sender) == str(target):
            self.refund("You can not challenge yourself.")

        if self.msg.value <= 0:
            self.refund("Bet must be grater than zero.")

        if self._myTarget[self.msg.sender] is not None:
            self.refund("You have already challanged someone.")

        if startTime <= self.current_time_seconds_tx():
            self.refund("Start time must be in the future! ")

        if self._myChallenger[target] is not None:

            # If the challenge starts time has already passed and wasn't accepted...
            if self.has_challenge_started(self._myChallengerStartTime[target]) and not self._myChallengerAccepted[target]:
                # at this point, the challenger send his bet and challenge,
                # but the target ignored him, so he gets an refund
                self.clear_my_challenger_with_payout(target)
            else:
                self.refund(
                    "This person has already a challenger. He must first accept/deny the request, before you can challenge.")

        self._challenge(target, startTime, duration)

    def _challenge(self, target: Address, startTime: int, duration: int):
        sender = self.msg.sender
        bet = self.msg.value

        self._myPoints[sender] = 0
        self._myPoints[target] = 0

        self._myTarget[sender] = target
        self._myChallenger[target] = sender
        self._myChallengerStartTime[target] = startTime
        self._myChallengerDuration[target] = duration
        self._myChallengersBet[target] = int(str(bet))
        self._myChallengerAccepted[target] = False

    def clear_my_challenger_with_payout(self, target: Address):
        # When the challenge has happened and is over
        if self._myChallengerAccepted[target] and self.has_challenge_ended(self._myChallengerStartTime[target], self._myChallengerDuration[target]):
            # pay out the bet to the winner

            points_target = self._myPoints[target]
            points_challenger = self._myPoints[self._myChallenger[target]]
            bet = self._myChallengersBet[target]

            if points_target > points_challenger:
                self.icx.send(target, bet * 2)
                self.set_last_challenge_result("The person who got challenged won with " + str(points_target) + " points. The challenger lost with " + str(points_challenger) + " points.", self._myChallenger[target], target)
            elif points_challenger > points_target:
                self.icx.send(self._myChallenger[target], bet * 2)
                self.set_last_challenge_result("The person who got challenged lost with " + str(points_target) + " points. The challenger won with " + str(points_challenger) + " points.", self._myChallenger[target], target)
            else:
                # no one has won... just refund the bets
                self.icx.send(target, bet)
                self.icx.send(self._myChallenger[target], bet)
                self.set_last_challenge_result("Draw with " + str(points_target) + " points on both side.", self._myChallenger[target], target)

        else:
            # refund challenger
            self.icx.send(self._myChallenger[target], self._myChallengersBet[target])

            # refund if target has paid the bet
            if self._myChallengerAccepted[target]:
                self.icx.send(target, self._myChallengersBet[target])

        self._myPoints.remove(self._myChallenger[target])
        self._myPoints.remove(target)

        self._myTarget.remove(self._myChallenger[target])
        self._myChallenger.remove(target)
        self._myChallengerStartTime.remove(target)
        self._myChallengerDuration.remove(target)
        self._myChallengersBet.remove(target)
        self._myChallengerAccepted.remove(target)

    def set_last_challenge_result(self, message: str, challenger: Address, target: Address):
        self._lastChallengeResult[challenger] = message
        self._lastChallengeResult[target] = message

    def refund(self, message: str):
        if self.msg.value > 0:
            self.icx.send(self.msg.sender, self.msg.value)
            message += " You got your " + str(self.msg.value) + " ICX refunded."

        revert(message)

    def is_challenge_running(self, startTime: int, duration: int) -> bool:
        return self.has_challenge_started(startTime) and not self.has_challenge_ended(startTime, duration)

    def has_challenge_started(self, startTime: int) -> bool:
        return self.current_time_seconds() >= startTime

    def has_challenge_ended(self, startTime: int, duration: int) -> bool:
        return self.current_time_seconds() >= startTime + duration

    def current_time_seconds_tx(self) -> int:
        # ! this returns time in nanoseconds 10^-9
        return int(round(self.tx.timestamp / 1000000000))

    def current_time_seconds(self) -> int:
        # ! this returns time in microseconds 10^-6
        return int(round(self.now() / 1000000))
