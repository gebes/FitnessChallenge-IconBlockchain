package eu.gebes;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import eu.gebes.ui.ConsoleUi;
import eu.gebes.ui.WalletLoader;
import eu.gebes.score.*;
import foundation.icon.icx.IconService;
import foundation.icon.icx.Wallet;
import foundation.icon.icx.crypto.KeystoreException;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.transport.http.HttpProvider;
import eu.gebes.utils.ConsoleUtils;

public final class App {


    final public static Address SCORE_ADDRESS = new Address("cx08e075d0585ac9d09615d7be8ec723344a95b3d5");

    public static void main(final String[] args) throws IOException, InvalidAlgorithmParameterException,
        NoSuchAlgorithmException, NoSuchProviderException, KeystoreException, InterruptedException {

        ConsoleUtils.clear();

        System.out.println("\nWelcome to the FitnessChallenge!");

        final IconService iconService = new IconService(new HttpProvider("https://bicon.net.solidwallet.io", 3));
        System.out.println("Conntected to testnet successfully!");

        ConsoleUtils.printLine();

        final WalletLoader walletLoader = new WalletLoader(new File("./wallets"));

        final Wallet myWallet = walletLoader.selectWallet();

        System.out.println("Successfully loaded your wallet (" + myWallet.getAddress() + ")");

        final ScoreCaller scoreCaller = new ScoreCaller(iconService, SCORE_ADDRESS, myWallet);
        final ConsoleUi consoleUi = new ConsoleUi(scoreCaller);
        consoleUi.home();

    }


}
