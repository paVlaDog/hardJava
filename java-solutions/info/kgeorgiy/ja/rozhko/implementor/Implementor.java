package info.kgeorgiy.ja.rozhko.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Class implementor
 */
// :NOTE: задокументирован не весь класс -- конструкторы и throws
public class Implementor implements Impler, JarImpler {
    /**
     * Construct new implementor
     */
    public Implementor(){
        super();
    }

    /**
     * The string that will be used in our implementation instead of a tab
     */
    private static final String TAB = "    ";

    /**
     * The string that will be used in our implementation instead of a line separator
     */
    private static final String LN = System.lineSeparator();

    /**
     * Runs {@code implement} or {@code jarImplement} depending on the number of arguments.
     * <p>
     * If the args[0] is <var>--jar</var>, then implementJar will be run with the args[1] and args[2],
     * otherwise - implement with the args[0] and args[1]
     *
     * @param args Arguments to implement and implement Jar, first the <var>--jar</var> flag (if exist), then the name of the
     *             inherited class, last the location.
     * @see Implementor#implement(Class, Path)
     * @see Implementor#implementJar(Class, Path)
     */
    public static void main(String[] args) {
        if (args != null && (args.length >= 3 || args.length >= 2 && !args[0].equals("--jar"))) {
            if (args[0] == null || args[1] == null || (args.length >= 3 && args[2] == null)) {
                System.err.println("Arg equals null");
            } else {
                try {
                    if (args[0].equals("--jar")) {
                        Class<?> myClass = Class.forName(args[1]);
                        new Implementor().implementJar(myClass, Path.of(args[2]));
                    } else {
                        Class<?> myClass = Class.forName(args[0]);
                        new Implementor().implement(myClass, Path.of(args[1]));
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Incorrect class ^_^");
                } catch (ImplerException e) {
                    System.err.println(e.getMessage());
                }
            }
        } else {
            System.err.println("Incorrect args count");
        }
    }

    /**
     * Produces code implementing class or interface specified by provided {@code token}.
     * <p>
     * Generated class' name should be the same as the class name of the type token with Impl suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * {@code root} directory and have correct file name. For example, the implementation of the
     * interface {@link List} should go to {@code $root/java/util/ListImpl.java}
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException when implementation cannot be
     *                         generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkClass(token);

        Path path;
        try {
            path = root.resolve(Path.of(token.getPackageName().replace(".", File.separator),
                    token.getSimpleName() + "Impl") + ".java");
        } catch (InvalidPathException e) {
            throw new ImplerException("Incorrect path");
        }
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            //
        }

        writeClassImplementation(token, path);
    }


    /**
     * Create a successor of the token class according to the rules described in {@code implement}.
     *
     * @param token type token to create implementation for.
     * @param path way to create implementation.
     * @throws ImplerException on class write error
     * @see Implementor#implement(Class, Path)
     */
    private void writeClassImplementation(Class<?> token, Path path) throws ImplerException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(String.format((token.getPackageName().length() != 0 ? "package %s;" : "") + LN + LN +
                            "public class %sImpl %s %s {" + LN, token.getPackageName(), token.getSimpleName(),
                    (token.isInterface() ? "implements " : "extends "), token.getCanonicalName()));
            for (Constructor<?> con : token.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(con.getModifiers())) {
                    writer.write(String.format(TAB + "%s%sImpl (%s) %s {" + LN + TAB + TAB + "super(%s);" + LN + TAB + "}" + LN,
                            getModifiersStroke(con.getModifiers()), token.getSimpleName(), getParametersStroke(con, true),
                            getExceptionsStroke(con), getParametersStroke(con, false)));
                }
            }

            Set<TrueMethod> abstractMethods = new HashSet<>();
            addExtendedMethod(token.getMethods(), abstractMethods);
            for (Class<?> iClass = token; iClass != null; iClass = iClass.getSuperclass()) {
                addExtendedMethod(iClass.getDeclaredMethods(), abstractMethods);
            }
            for (TrueMethod m : abstractMethods) {
                if (Modifier.isAbstract(m.getMethod().getModifiers())) {
                    implementMethods(writer, m.getMethod());
                }
            }
            writer.write("}");
        } catch (IOException e) {
            throw new ImplerException("Writer in " + path + " error");
        }
    }

    /**
     * Checks if a class can be inherited, throws an error otherwise
     *
     * @param token class to be tested
     * @throws ImplerException if it is impossible to create an heir
     */
    private void checkClass(Class<?> token) throws ImplerException {
        if (token.isPrimitive() || token.isArray() || token == Enum.class) throw new ImplerException(token + "class don't implementable");
        if (Modifier.isPrivate(token.getModifiers())) throw new ImplerException("Private class don't implementable");
        if (Modifier.isFinal(token.getModifiers())) throw new ImplerException("Private class don't implementable");
        boolean existNoPrivateMethod = false;
        for (Constructor<?> con : token.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(con.getModifiers())) {
                existNoPrivateMethod = true;
                break;
            }
        }
        if (!token.isInterface() && !existNoPrivateMethod) {
            throw new ImplerException(token + "class don't implementable");
        }
    }

    /**
     * Selects from the passed methods only those that can be implemented in the extended class
     *
     * @param methods a set of methods from which suitable ones will be selected
     * @param ansMethods the set into which suitable methods will be added
     * @throws NullPointerException if method in methods is null
     */

    private void addExtendedMethod(Method[] methods, Set<TrueMethod> ansMethods) {
        for (Method m : methods) {
            ansMethods.add(new TrueMethod(m));
        }
    }


    /**
     * Returns all constructor exceptions as a string in java code style.
     *
     * @param con constructor to get exceptions from
     * @return java style string with all exceptions in this constructor
     */

    private String getExceptionsStroke(Constructor<?> con) {
        return con.getExceptionTypes().length != 0 ? "throws " + Arrays.stream(con.getExceptionTypes())
                .map(Class::getCanonicalName).collect(Collectors.joining(", ")) : "";
    }

    /**
     * Returns all {@code Executable} element parameters as a string in java code style.
     *
     * @param el {@code Executable} element to get {@code Parameter} from
     * @param withType True if it is necessary to infer the types of the parameters and false otherwise
     * @return java style string with all Parameters in this {@code Executable} element
     * @see Executable
     * @see Parameter
     * @throws MalformedParametersException – if the class file contains a MethodParameters attribute that is improperly formatted.
     */

    private String getParametersStroke(Executable el, boolean withType) {
        return Arrays.stream(el.getParameters()).map(x -> !withType ? x.getName()
                : x.getType().getCanonicalName() + " " + x.getName()).collect(Collectors.joining(", "));
    }

    /**
     * Returns all modifiers as a string in java code style.
     *
     * @param modifiersCode code describing available modifiers
     * @return java style string with all modifiers in this modifiersCode
     */

    private String getModifiersStroke(int modifiersCode) {
        return Modifier.toString(modifiersCode&~Modifier.TRANSIENT&~Modifier.ABSTRACT) + " ";
    }

    /**
     * Returns method element parameters as a string in java code style and write in transferred writer
     *
     * @param writer the writer in which the method will be written
     * @param m writable method
     * @throws IOException when a writer error occurs
     * @throws MalformedParametersException – – if the class m file contains a MethodParameters attribute
     * that is improperly formatted.
     */

    private void implementMethods(BufferedWriter writer, Method m) throws IOException {
        writer.write(String.format(TAB + "%s%s %s (%s) {" + LN + TAB + TAB + "%s" + LN + TAB + "}" + LN + LN,
                getModifiersStroke(m.getModifiers()), m.getReturnType().getCanonicalName(), m.getName(),
                getParametersStroke(m, true), getReturnTypeStroke(m.getReturnType())));
    }

    /**
     * Returns returnType as a string in java code style for return in method.
     *
     * @param m return type, may be primitive
     * @return Returns a string like "return default-value". For void, it returns an empty string.
     */

    private String getReturnTypeStroke(Class<?> m) {
        if (!m.isPrimitive()) {
            return "return null;";
        }
        return switch (m.descriptorString()) {
            case "Z" -> "return false;";
            case "V" -> "";
            default -> "return 0;";
        };
    }

    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class' name should be the same as the class name of the type token with <var>Impl</var> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when an implementation cannot be generated or a compile/archive error has occurred
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path parentPath = jarFile.toAbsolutePath().getParent();
        if (parentPath == null) {
            throw new ImplerException("Incorrect path, don't have parent");
        }
        try {
            Files.createDirectories(parentPath);
        } catch (IOException e) {
            //
        }

        Path tempPath = null;
        try {
            tempPath = Files.createTempDirectory(parentPath.toAbsolutePath(), "jar-implementor");
            if (tempPath == null) {
                throw new ImplerException("unable to create temporal directory");
            }
            implement(token, tempPath);
            String classPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
            compileJava(token, tempPath, classPath);
            createJar(token, jarFile, tempPath);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        } catch (IOException e) {
            throw new ImplerException("Error create temp directory");
        } finally {
            if (tempPath != null) {
                try {
                    Files.walkFileTree(tempPath.toAbsolutePath(), new DeleteFileVisitor());
                } catch (IOException e) {
                    System.err.println("Error deleting temp file - " + tempPath.toAbsolutePath() + ". Please, delete manually.");
                }
            }
        }
    }

    /**
     * Compiles the specified class at the given {@code path} and with the given {@code classpath}
     *
     * @param token class to be compiled
     * @param path directory for compilation
     * @param classPath classpath used as an argument during compilation
     * @throws ImplerException if compilation fails or compiler don't exist
     * @throws InvalidPathException if the path to the token does not exist
     */

    private void compileJava(Class<?> token, Path path, String classPath) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] args = new String[]{"-encoding", "UTF-8", "-cp", path + File.pathSeparator + classPath,
                path.resolve(getNameWithPackage(token, File.separator) + ".java").toString()};
        if (compiler == null || compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("failed to compile generated file");
        }
    }

    /**
     * Creates a .jar file from the classes in the directory {@code temp} along the path {@code jarFile}
     * with the passed {@code classPath} as MANIFEST.MF.
     *
     * @param token the class from which the compiled files were received
     * @param jarPath the path along which the future jarFile should be located
     * @param classDir the directory where the compiled classes are located
     * @throws ImplerException if an error occurs while opening or writing to an output stream
     */

    private void createJar(Class<?> token, Path jarPath, Path classDir) throws ImplerException {
        Manifest MF = new Manifest();
        MF.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath), MF)) {
            String name = getNameWithPackage(token, "/") + ".class";
            out.putNextEntry(new ZipEntry(name));
            Files.copy(classDir.resolve(name), out);
        } catch (IOException e) {
            throw new ImplerException("Error occurred while creating jar file");
        }
    }

    /**
     * Returns the name with package to the new implementation along with the package.
     *
     * @param token the class whose full name of the successor we are looking for
     * @param separator separator to be used in the name with package
     * @return a string consisting of the package with the selected delimiters and the class name
     */

    private String getNameWithPackage (Class<?> token, String separator) {
        return token.getPackageName().replace(".", separator) + separator + token.getSimpleName() + "Impl";
    }
}
