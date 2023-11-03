#!/bin/bash
cd ..
javadoc -d javadoc \
-link  https://docs.oracle.com/en/java/javase/20/docs/api/ \
-private \
-cp java-advanced-2023/lib:../java-advanced-2023/artifacts \
java-solutions/info/kgeorgiy/ja/rozhko/implementor/*.java \
java-advanced-2023/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/*Impler*.java
sleep 20
