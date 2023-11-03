javac -cp C:\All\Stu\JavaAdvanced\java-advanced\java-advanced-2023\artifacts\info.kgeorgiy.java.advanced.implementor.jar info\kgeorgiy\ja\rozhko\implementor\*.java
javac info\kgeorgiy\ja\rozhko\implementor\*.java
jar -cfm app.jar MANIFEST.txt info\kgeorgiy\ja\rozhko\implementor\*.class
java -jar app.jar info.kgeorgiy.ja.rozhko.implementor.BeautifulPrinter C:\All\Stu\JavaAdvanced\java-advanced\java-solutions
