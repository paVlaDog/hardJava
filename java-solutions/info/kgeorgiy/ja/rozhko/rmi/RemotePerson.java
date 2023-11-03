package info.kgeorgiy.ja.rozhko.rmi;

import java.util.HashMap;
import java.util.Map;

public class RemotePerson extends AbstractPerson{
    public RemotePerson(final String name, final String surname, final String passportID) {
        super(name, surname, passportID, new HashMap<>());
    }

    @Override
    public synchronized Map<String, Account> getAccounts() {
        return accounts;
    }
}
