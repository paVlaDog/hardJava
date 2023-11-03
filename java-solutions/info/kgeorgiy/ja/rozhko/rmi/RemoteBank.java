package info.kgeorgiy.ja.rozhko.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    public Person createPerson(final String name, final String surname, final String passportID) throws RemoteException {
        System.out.println("Creating person " + passportID);
        final RemotePerson person = new RemotePerson(name, surname, passportID);
        if (persons.putIfAbsent(passportID, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getPerson(passportID, false);
        }
    }

    public Person getPerson(final String passwordID, final boolean local) {
        System.out.println("Retrieving person " + passwordID);
        try {
            if (persons.containsKey(passwordID)) {
                return local ? new LocalPerson(persons.get(passwordID)) : persons.get(passwordID);
            } else {
                return null;
            }
        } catch (RemoteException e) {
            throw new RuntimeException("Unknown error" + e);
        }
    }

    public Account createAccountForPerson(final Person person, final String subId) {
        try {
            String passportID = person.getPassportID();
            System.out.println("Creating account " + subId + " for person " + passportID);
            String id = passportID + ":" + subId;
            if (accounts.containsKey(passportID + subId)) {
                Account account = accounts.get(id);
                if (person.getAccounts().containsValue(account)) {
                    return account;
                } else {
                    System.out.println("Unknown error - exist account " + id + ", but not exist account "
                            + subId + "for person" + passportID);
                    return null;
                }
            } else {
                Account account = createAccount(id);
                if (person.getAccounts().containsKey(subId)) {
                    System.out.println("Unregistered account " + subId + "in person " + passportID + ". Removal.");
                }
                person.getAccounts().put(subId, account);
                return account;
            }
        } catch (RemoteException e) {
            throw new RuntimeException("Unknown error" + e);
        }
    }
}
