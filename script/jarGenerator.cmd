javac ..\java-solutions\info\kgeorgiy\ja\rozhko\implementor\Implementor.java
    -d . -cp ..\java-advanced-2023\modules\info.kgeorgiy.java.advanced.implementor;
    ..\java-advanced\java-solutions
jar -cfm app.jar MANIFEST.txt info
java -jar app.jar arg1 arg2
sleep 10
