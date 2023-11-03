package info.kgeorgiy.ja.rozhko.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public interface Person extends Remote {
    String getName() throws RemoteException;
    String getSurname() throws RemoteException;
    String getPassportID() throws RemoteException;
    Map<String, Account> getAccounts() throws RemoteException;
}
