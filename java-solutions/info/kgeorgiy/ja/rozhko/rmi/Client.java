package info.kgeorgiy.ja.rozhko.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public final class Client {
    /** Utility class. */
    private Client() {}

    public static void main(final String... args) throws RemoteException {
        if (args == null) {
            System.out.println("Args is null");
        } else if (args.length < 5) {
            System.out.println("Args length < 5");
        } else if (IntStream.range(0, 5).anyMatch((i) -> Objects.isNull(args[i]))) {
            System.out.println("Args element is null");
        } else {
            try {
                int addMoney = Integer.parseInt(args[4]);
                final Bank bank = (Bank) LocateRegistry.getRegistry(228).lookup("//localhost/bank");
                Person person = bank.createPerson(args[0], args[1], args[2]);
                if (!person.getName().equals(args[0]) || !person.getSurname().equals(args[1])) {
                    System.out.println("Incorrect name or surname for this passportID");
                }
                Account account = bank.createAccountForPerson(person, args[3]);
                account.setAmount(addMoney);
                System.out.println("Account money:" + account.getAmount());
            } catch (NumberFormatException e) {
                System.out.println("Fifth argument must be integer, args[4] = " + args[4]);
            } catch (final NotBoundException e) {
                System.out.println("Bank is not bound");
            } catch (RemoteException e) {
                System.err.println("Remote error " + e.getMessage());
            }
        }

    }
}
