package info.kgeorgiy.ja.rozhko.rmi;

import java.rmi.RemoteException;
import java.util.*;

public class LocalPerson extends AbstractPerson{
    public LocalPerson(RemotePerson person) throws RemoteException {
        super(person.getName(), person.getSurname(), person.getPassportID(), new HashMap<>());
        for (var el : person.getAccounts().entrySet()) {
            accounts.put(el.getKey(), new LocalAccount(el.getValue()));
        }
//        accounts = newAccounts(person);
    }

//    private Map<String, Account> newAccounts(RemotePerson person) throws RemoteException {
//        Map <String, Account> localAccounts = new HashMap<>();
//        for (var el : person.getAccounts().entrySet()) {
//            localAccounts.put(el.getKey(), new LocalAccount(el.getValue()));
//        }
//        return localAccounts;
//    }

    @Override
    public Map<String, Account> getAccounts() {
        return accounts;
    }
}
