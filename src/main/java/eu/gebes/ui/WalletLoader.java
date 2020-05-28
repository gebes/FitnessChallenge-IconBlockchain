package eu.gebes.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import eu.gebes.utils.OptionSelector;
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

        final File[] selectables =  Objects.requireNonNull(walletsCollection.listFiles());

        OptionSelector<File> selector = new OptionSelector<>();

        Arrays.stream(selectables).filter(file -> !file.isDirectory()).forEachOrdered((file -> selector.addOption(file.getName(), file)));

        File selected = selector.select("Select a wallet: ");

        // ! Its more handy to save the password in the filename

        try {
            return KeyWallet.load(selected.getName(), selected);
        } catch (IOException | KeystoreException e) {
            System.out.println("Couldnt load your wallet. Is the password the filename??");
            throw new RuntimeException(e);
        }

    }

}
