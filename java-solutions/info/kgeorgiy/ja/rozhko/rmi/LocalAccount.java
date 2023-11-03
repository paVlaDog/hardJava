package info.kgeorgiy.ja.rozhko.rmi;

import java.rmi.RemoteException;

public class LocalAccount extends AbstractAccount {
    public LocalAccount(final Account account) throws RemoteException {
        super(account.getId(), account.getAmount());
    }

    @Override
    public int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
