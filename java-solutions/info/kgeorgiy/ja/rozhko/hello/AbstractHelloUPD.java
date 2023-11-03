package info.kgeorgiy.ja.rozhko.hello;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Abstract class for rendering common HelloUDP methods
 *
 * @author paVlaDog
 */
public abstract class AbstractHelloUPD {
    protected Charset CHARSET = StandardCharsets.UTF_8;
    protected int TIMEOUT = 50;
}
