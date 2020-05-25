package eu.gebes.ui;


import eu.gebes.score.Challenger;
import eu.gebes.score.ScoreCaller;
import eu.gebes.utils.ConsoleUtils;
import eu.gebes.utils.OptionSelector;
import foundation.icon.icx.data.Address;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Scanner;


@RequiredArgsConstructor
public class ConsoleUi {

    @NonNull ScoreCaller scoreCaller;

    public void home() {

        OptionSelector<Runnable> selector = new OptionSelector<>();

        selector.addOption("Get my challenger", this::showMyCurrentChallenger);
        selector.addOption("Challenge someone", this::challengeSomeone);


        while (true) {
            ConsoleUtils.printLine();
            selector.select("Choose an option: ").run();
        }

    }


    public void showMyCurrentChallenger() {

        Challenger challenger = scoreCaller.getMyChallenger();

        if (challenger == null) {
            System.out.println("Currently you do not have any challenger :c");
        } else {
            System.out.println("You have one Challenger");
            System.out.println("  > Address: " + challenger.getAddress());
            System.out.println("  > Bet: " + challenger.getBet());
        }


    }

    public void challengeSomeone() {

        System.out.print("Enter the target address: ");
        Address address = new Address(new Scanner(System.in).nextLine());
        System.out.print("Enter your bet: ");
        BigInteger bet = new BigInteger(new Scanner(System.in).nextLine());

        scoreCaller.challenge(address, bet);


    }

}
