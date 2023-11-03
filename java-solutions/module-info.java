module info.kgeorgiy.ja.rozhko {
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.concurrent;
    requires java.compiler;
    requires info.kgeorgiy.java.advanced.mapper;
    requires info.kgeorgiy.java.advanced.crawler;
    requires info.kgeorgiy.java.advanced.hello;
    requires jdk.httpserver;
    requires java.rmi;
    requires junit;

    exports info.kgeorgiy.ja.rozhko.implementor;
    exports info.kgeorgiy.ja.rozhko.concurrent;
    exports info.kgeorgiy.ja.rozhko.student;
    exports info.kgeorgiy.ja.rozhko.arrayset;
    exports info.kgeorgiy.ja.rozhko.walk;
    exports info.kgeorgiy.ja.rozhko.rmi;
    exports info.kgeorgiy.ja.rozhko.i18n;

    opens info.kgeorgiy.ja.rozhko.implementor to info.kgeorgiy.java.advanced.implementor;
    opens info.kgeorgiy.ja.rozhko.concurrent to info.kgeorgiy.java.advanced.concurrent;
    opens info.kgeorgiy.ja.rozhko.arrayset to info.kgeorgiy.java.advanced.arrayset;
    opens info.kgeorgiy.ja.rozhko.walk to info.kgeorgiy.java.advanced.walk;
    opens info.kgeorgiy.ja.rozhko.student to info.kgeorgiy.java.advanced.student;
}