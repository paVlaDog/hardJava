package info.kgeorgiy.ja.rozhko.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Wrapper over the Method class that redefines {@code equal} and {@code hashCode} so that methods with the same name,
 * type parameters, and return types are considered the same
 * @see TrueMethod#equals(Object)
 * @see TrueMethod#hashCode()
 */
public class TrueMethod {

    /**
     * The method over which the wrapper was created
     */
    private final Method method;

    /**
     * getter for internal method
     * @return {@code method}
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Standard constructor for wrapper
     * @param method internal method
     */
    public TrueMethod(Method method) {
        this.method = method;
    }

    /**
     * Redefined operator {@link Method#equals(Object)} comparing only name, argument type and return type
     *
     * @param obj Another method we will be comparing with
     * @return true if the above parameters are equal and false otherwise
     * @see Object#equals(Object)
     * @see Method#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrueMethod other) {
            if (method.getName().equals(other.getMethod().getName())) {
                if (!method.getReturnType().equals(other.getMethod().getReturnType()))
                    return false;
                if (method.getParameterTypes().length == other.getMethod().getParameterTypes().length) {
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        if (method.getParameterTypes()[i] != other.getMethod().getParameterTypes()[i])
                            return false;
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Redefined operator {@link Method#hashCode()} comparing only name, argument type and return type
     *
     * @return {@code Objects.hash} value, whose calculation is based on the parameters listed above
     * @see Objects#hash(Object...)
     */
    @Override
    public int hashCode() {
        return Objects.hash(method.getName(), Arrays.hashCode(method.getParameterTypes()));
    }
}
