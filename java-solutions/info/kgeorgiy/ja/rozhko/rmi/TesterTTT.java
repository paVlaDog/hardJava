package info.kgeorgiy.ja.rozhko.rmi;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TesterTTT {

    @Test
    public void test1_Correct_Name_Surname_Passport() {
        try {
            Server.main();
            final Bank bank = (Bank) LocateRegistry.getRegistry(228).lookup("//localhost/bank");
            Person ilon = bank.createPerson("Ilon", "Mask", "Mars");
            Assert.assertEquals(ilon.getName(), "Ilon");
            Assert.assertEquals(ilon.getSurname(), "Mask");
            Assert.assertEquals(ilon.getPassportID(), "Mars");
            Person ilon2 = bank.getPerson("Mars", false);
            Assert.assertEquals(ilon2.getName(), "Ilon");
            Assert.assertEquals(ilon2.getSurname(), "Mask");
            Assert.assertEquals(ilon2.getPassportID(), "Mars");
            Person ilon3 = bank.getPerson("Mars", false);
            Assert.assertEquals(ilon3.getName(), "Ilon");
            Assert.assertEquals(ilon3.getSurname(), "Mask");
            Assert.assertEquals(ilon3.getPassportID(), "Mars");
        } catch (RemoteException e) {

        } catch (NotBoundException e) {

        }
    }

    @Test
    public void test2() {
        try {
            Server.main();
            final Bank bank = (Bank) LocateRegistry.getRegistry(228).lookup("//localhost/bank");
            Person knazz = bank.createPerson("Andrey", "Knyazev", "sorcerer");
            Account accountKnazz = bank.createAccountForPerson(knazz, "doll");
            accountKnazz.setAmount(100);
            Assert.assertEquals(accountKnazz.getAmount(), 100);
            accountKnazz.setAmount(accountKnazz.getAmount() + 1000);
            Assert.assertEquals(accountKnazz.getAmount(), 1100);
            Person imitator = bank.getPerson("sorcerer", true);
            Account accountImitator = bank.createAccountForPerson(knazz, "witch");
            accountImitator.setAmount(-100);
            Assert.assertEquals(accountKnazz.getAmount(), 1100);
            Assert.assertEquals(accountImitator.getAmount(), -100);
        } catch (RemoteException e) {

        } catch (NotBoundException e) {

        }
    }




}
