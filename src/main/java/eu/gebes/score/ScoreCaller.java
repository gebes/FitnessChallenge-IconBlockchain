package eu.gebes.score;

import java.io.IOException;
import java.math.BigInteger;

import foundation.icon.icx.*;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.data.TransactionResult;
import foundation.icon.icx.transport.jsonrpc.RpcItemCreator;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ScoreCaller {

    @NonNull
    IconService iconService;
    @NonNull
    Address scoreAddress;
    @NonNull
    Wallet myWallet;


    Helper helper = new Helper();


    public void challenge(Address target, BigInteger bet, BigInteger startTime, BigInteger duration) {

        var transaction = TransactionBuilder.newBuilder()
                .nid(new BigInteger("3"))
                .from(getMyWallet().getAddress())
                .to(scoreAddress)
                .value(bet)
                .stepLimit(new BigInteger("10000000"))
                .nonce(new BigInteger("1"))
                .call("challenge")
                .params(new RpcObject.Builder().put("target", RpcItemCreator.create(target)).put("startTime", RpcItemCreator.create(startTime)).put("duration", RpcItemCreator.create(duration)).build())
                .build();


        var result = helper.getUnsignedTransactionResult(transaction, getMyWallet());
        helper.printResult(result);
    }


    public void acceptChallenge(Challenger challenger) {

        var transaction = TransactionBuilder.newBuilder()
                .nid(new BigInteger("3"))
                .from(getMyWallet().getAddress())
                .to(scoreAddress)
                .value(challenger.getBet())
                .stepLimit(new BigInteger("10000000"))
                .nonce(new BigInteger("1"))
                .call("accept")
                .build();


        var result = helper.getUnsignedTransactionResult(transaction, getMyWallet());
        helper.printResult(result);
    }

    public void denyChallenge() {

        var transaction = TransactionBuilder.newBuilder()
                .nid(new BigInteger("3"))
                .from(getMyWallet().getAddress())
                .to(scoreAddress)
                .stepLimit(new BigInteger("10000000"))
                .nonce(new BigInteger("1"))
                .call("deny")
                .build();


        var result = helper.getUnsignedTransactionResult(transaction, getMyWallet());
        helper.printResult(result);
    }


    public Challenger getMyChallenger() {

        var call = new Call.Builder().from(myWallet.getAddress()).to(scoreAddress).method("get_my_challengers")
                .buildWith(String.class);

        try {
            String result = iconService.call(call).execute();

            if (result.equals("None"))
                return null;

            String[] split = result.split(":", 4);
            return new Challenger(new BigInteger(split[0]), new Address(split[1]), new BigInteger(split[2]), new BigInteger(split[3]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * * All Helper Methods are stored here
     */
    class Helper {

        public void printResult(TransactionResult result) {
            if (helper.didFail(result)) {
                // Failure Object is not null when there is an error
                System.out.println("Transaction failed with message: " + result.getFailure().getMessage());
                return;

            }
            System.out.println("Transaction finished successfully");
        }

        public boolean didFail(TransactionResult result) {
            return result.getFailure() != null;
        }

        /**
         * @param unsigned     transactions which gets signed
         * @param walletToSign wallet which is used for being signed
         * @return the result after transaction is done
         */
        public TransactionResult getUnsignedTransactionResult(Transaction unsigned, Wallet walletToSign) {
            var signedTransaction = new SignedTransaction(unsigned, walletToSign);

            try {
                var transactionId = iconService.sendTransaction(signedTransaction).execute();
                return getTransactionResult(transactionId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        public TransactionResult getTransactionResult(Bytes transaction) {
            TransactionResult result = null;

            // * Try as long as the transaction is pending
            while (result == null) {
                try {
                    result = iconService.getTransactionResult(transaction).execute();
                } catch (IOException ignored) {
                }
            }
            return result;
        }
    }

}
