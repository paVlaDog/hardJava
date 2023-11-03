package info.kgeorgiy.ja.rozhko.rmi;

public class RemoteAccount extends AbstractAccount {
    public RemoteAccount(final String id) {
        super(id, 0);
    }

    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
