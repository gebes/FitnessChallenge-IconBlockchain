package eu.gebes.ui;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.Wallet;
import foundation.icon.icx.crypto.KeystoreException;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import eu.gebes.utils.ConsoleUtils;

@Data
@RequiredArgsConstructor
public class WalletLoader {

    @NonNull
    File walletsCollection;

    public Wallet selectWallet() {

        final File[] selectables = walletsCollection.listFiles();

        for (int i = 0; i < selectables.length; i++)
            System.out.println("\t" + i + ": " + selectables[i].getName());

        int selectedWallet = ConsoleUtils.scanIntRange("Select a wallet: ", 0, selectables.length-1);

        // ! If you selected a default wallet, the name of the file is the password
        System.out.print("Enter the password for the wallet: ");
        final String password = new Scanner(System.in).nextLine();

        try {
            return KeyWallet.load(password, selectables[selectedWallet]);
        } catch (IOException | KeystoreException e) {
            System.out.println("Couldnt load your wallet :c");
            throw new RuntimeException(e);
        }

    }

}
