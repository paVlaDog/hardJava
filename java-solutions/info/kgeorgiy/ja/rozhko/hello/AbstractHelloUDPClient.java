package info.kgeorgiy.ja.rozhko.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Abstract class for rendering common client methods
 *
 * @author paVlaDog
 */
public abstract class AbstractHelloUDPClient extends AbstractHelloUPD implements HelloClient {
    protected static void subMain(HelloClient instanse, String[] args) {
        if (args == null) {
            System.err.println("Args is null");
        } else if (IntStream.range(0, 5).anyMatch(i -> Objects.isNull(args[i]))) {
            System.err.println("Args[i] is null");
        } else if (args.length < 5) {
            System.err.println("Length of args < 5");
        } else {
            try {
                HelloClient client = instanse;
                client.run(args[0],
                        Integer.parseInt(args[1]),
                        args[2],
                        Integer.parseInt(args[3]),
                        Integer.parseInt(args[4]));
            } catch (NumberFormatException e) {
                System.err.println("Args 2, 3 or 5 is not integer");
            }
        }
    }

    protected boolean ansHandler(String responseString, String requestStroke, int threadNumber, int requestNumber) {
        List<Integer> list = getAllNumbers(responseString);
        if (list.size() == 2 && list.get(0) == threadNumber && list.get(1) == requestNumber) {
            System.out.println(requestStroke + System.lineSeparator() + responseString);
            return true;
        }
        return false;
    }

    protected List<Integer> getAllNumbers(String stroke) {
        List <Integer> list = new ArrayList<>();
        int begin = 0, end = 0;
        while (end != stroke.length()) {
            for (;stroke.length() - 1 >= end && !Character.isDigit(stroke.charAt(begin)); begin++, end++){}
            for (;stroke.length() - 1 >= end && Character.isDigit(stroke.charAt(end)); end++){}
            if (begin != end) {
                list.add(Integer.parseInt(stroke.substring(begin, end)));
                begin = end;
            }
        }
        return list;
    }

    protected String requestStrokeGenerate(String prefix, int threadNumber, int requestNumber) {
        return prefix + threadNumber + "_" + requestNumber;
    }
}
