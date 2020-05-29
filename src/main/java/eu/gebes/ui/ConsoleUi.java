package eu.gebes.ui;


import eu.gebes.App;
import eu.gebes.score.Challenge;
import eu.gebes.score.Challenger;
import eu.gebes.score.ScoreCaller;
import eu.gebes.utils.ConsoleUtils;
import eu.gebes.utils.DateUtils;
import eu.gebes.utils.OptionSelector;
import foundation.icon.icx.crypto.KeystoreException;
import foundation.icon.icx.data.Address;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Scanner;


@RequiredArgsConstructor
public class ConsoleUi {

    @NonNull ScoreCaller scoreCaller;

    public void home() {

        OptionSelector<Runnable> selector = new OptionSelector<>();

        selector.addOption("Get my challenger", this::showMyCurrentChallenger);
        selector.addOption("Challenge someone", this::challengeSomeone);
        selector.addOption("Has my target accepted?", this::hasMyTargetAccepted);
        selector.addOption("Submit Fitness Points (only during challenge)", this::submitFitnessPoints);
        selector.addOption("Result of the last challenge", this::lastChallengeResult);
        selector.addOption("Log Out", () -> {
            App.main(new String[]{});
        });


        while (true) {
            ConsoleUtils.printLine();
            selector.select("Choose an option: ").run();
        }

    }


    public void lastChallengeResult() {

        String result = scoreCaller.getLastChallengeResult();


        System.out.println(result);

    }

    public void submitFitnessPoints() {

        System.out.print("Enter amount of points: ");
        BigInteger amount = new BigInteger(new Scanner(System.in).nextLine());

        scoreCaller.submitPoints(amount);

    }

    public void showMyCurrentChallenger() {

        Challenger challenger = scoreCaller.getMyChallenger();

        if (challenger == null) {
            System.out.println("Currently you do not have any challenger :c");
        } else {
            System.out.println("You have one Challenger");
            System.out.println("  > Address: " + challenger.getAddress());
            System.out.println("  > Bet: " + challenger.getBet());
            System.out.println("  > StartTime: " + DateUtils.convertUnixSecondsToDate(challenger.getStartTime()));
            System.out.println("  > Ends: " + DateUtils.convertUnixSecondsToDate(challenger.getStartTime().add(challenger.getDuration())));


            var selector = new OptionSelector<Runnable>();

            selector.addOption("Accet", () -> accept(challenger));
            selector.addOption("Deny", this::deny);


            selector.select("Choose an otpion: ").run();

        }


    }

    public void hasMyTargetAccepted() {

        Challenge challenge = scoreCaller.hasMyTargetAccepted();

        if (challenge == null) {
            System.out.println("Currently you do not have challenged anyone");
        } else {
            System.out.println("Current state:");
            System.out.println("  > Address: " + challenge.getTarget());
            System.out.println("  > hasAccepted: " + challenge.isAccepted());
            System.out.println("  > StartTime: " + DateUtils.convertUnixSecondsToDate(challenge.getStartTime()));
            System.out.println("  > Ends: " + DateUtils.convertUnixSecondsToDate(challenge.getStartTime().add(challenge.getDuration())));


        }

    }

    public void accept(Challenger challenger) {
        scoreCaller.acceptChallenge(challenger);
    }

    public void deny() {
        scoreCaller.denyChallenge();
    }


    public void challengeSomeone() {

        System.out.print("Enter the target address: ");
        Address address = new Address(new Scanner(System.in).nextLine());
        System.out.print("Enter your bet: ");
        BigInteger bet = new BigInteger(new Scanner(System.in).nextLine());
        long unix = System.currentTimeMillis() / 1000;
        System.out.print("Enter StartTime in UNIX & Seconds (current is " + unix + " seconds): ");
        BigInteger startTime = new BigInteger(new Scanner(System.in).nextLine());
        System.out.print("Enter Duration in Seconds: ");
        BigInteger duration = new BigInteger(new Scanner(System.in).nextLine());

        scoreCaller.challenge(address, bet, startTime, duration);

    }

}
