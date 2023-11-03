package info.kgeorgiy.ja.rozhko.rmi;

import java.util.Map;

public abstract class AbstractPerson implements Person {
    protected final String name;
    protected final String surname;
    protected final String passportID;
    protected Map<String, Account> accounts;

    public AbstractPerson(final String name,
                        final String surname,
                        final String passportID,
                          final Map<String, Account> accounts) {
        this.name = name;
        this.surname = surname;
        this.passportID = passportID;
        this.accounts = accounts;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassportID() {
        return passportID;
    }


}
