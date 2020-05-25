from ..fitness_score import FitnessScore
from tbears.libs.scoretest.score_test_case import ScoreTestCase


class TestFitnessScore(ScoreTestCase):

    def setUp(self):
        super().setUp()
        self.score = self.get_score_instance(FitnessScore, self.test_account1)

    def test_hello(self):
        self.assertEqual(self.score.hello(), "Hello")
