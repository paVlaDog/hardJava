package info.kgeorgiy.ja.rozhko.rmi;

public abstract class AbstractAccount implements Account {
    protected final String id;
    protected int amount;

    public AbstractAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }
}
