package boot;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Zip files must have the following directory
 *
 * META-INF/MANIFEST.MF with a classpath entry of the form
 *     Main-Class: boot.ZipStarter
 *     ZipStarter-Main-Class: path-to-Main-Class-of-my-program
 *
 * they can have the following directories:
 *   lib/*.jar
 *   classes/[package]/.../[name].class
 *
 * the classfiles of classes and the jars of the lib directories will be added to the classpath
 *
 */
public class ZipStarter extends ClassLoader {

    private Map<URL, byte[]>       resourceStore;
    private Map<String, List<URL>> pathname2resources;

    public static void main (String... args) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        final String mainClassKey = "ZipStarter-Main-Class";
        final String classPathKey = "ZipStarter-Class-Path";

        String mainClass = System.getProperty(mainClassKey);
        String classPath = System.getProperty(classPathKey);
        Manifest mf = null;
        if (mainClass == null) {
            try {
                mf = new Manifest(ZipStarter.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
                mainClass = mf.getMainAttributes().getValue(mainClassKey);
                if (mainClass == null)
                    throw new IOException("There's no valid '" + mainClassKey + "' key!");
            } catch (IOException ex) {
                System.err
                      .println("There is no 'ZipStarter-Main-Class' Parameter. Neither as system property (java -jar xy.zip -DZipStarter-Main-Class=Main) nor as element (ZipStarter-Main-Class) inside META-INF/MANIFEST.MF");
                System.exit(-1);
            }
        }
        if (classPath == null) {
            try {
                if(mf == null)
                    mf = new Manifest(ZipStarter.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
                classPath = mf.getMainAttributes().getValue(classPathKey);
                if(classPath == null) {
                    System.err.println("There is no 'ZipStarter-Class-Path' Parameter. Neither as system property (java -jar xy.zip -DZipStarter-Class-Path=**) nor as element "
                                        + "(ZipStarter-Class-Path) inside META-INF/MANIFEST.MF");
                    System.err.println("Using '/libs' and '/classes' as default class path directories.");
                    classPath = "/libs:/classes";
                } else {
                    classPath = classPath.replace(" ",":");
                }
            } catch (IOException ex) {
                System.err
                      .println("There is no 'ZipStarter-Class-Path' Parameter. Neither as system property (java -jar xy.zip -DZipStarter-Class-Path=/libs:/classes) nor as element "
                               + "(ZipStarter-Class-Path) inside META-INF/MANIFEST.MF");
                System.err.println("Using '/libs' and '/classes' as default class path directories.");
                classPath = "/libs:/classes";
            }
        }

        ZipStarter zs = new ZipStarter();

        // call boot.zip.Handler.register(ClassLoader zipStarter)
        Class<?> clazz = ZipStarter.class.getClassLoader().loadClass("boot.zip.Handler");
        Class[] argTypes = new Class[] {ZipStarter.class};
        Method method = clazz.getDeclaredMethod("registerHandler", argTypes);
        method.invoke(null, (Object) zs);

        String zipFilePath = ZipStarter.class.getResource("/boot").getFile();
        zipFilePath = zipFilePath.substring(0, zipFilePath.lastIndexOf("!"));
        zipFilePath = zipFilePath.substring("file:".length());

        File zipFile = new File(zipFilePath);
        zs.init(zipFile, classPath);

        // call the zip-main class of this zip
        clazz = zs.loadClass(mainClass);
        argTypes = new Class[] {String[].class};
        method = clazz.getDeclaredMethod("main", argTypes);
        method.invoke(null, (Object) args);
    }

    public ZipStarter () {
        super(ZipStarter.class.getClassLoader());
    }

    public ZipStarter (ClassLoader parent) {
        super(parent);
    }

    public void init (File zipFile, String classPath) throws IOException {
        InputStream zip = null;
        try {
            zip = new FileInputStream(zipFile);
            init(zip, classPath);
        } finally {
            if (zip != null)
                zip.close();
        }

    }

    public void init (InputStream zipFile, String classPath) throws IOException {
        resourceStore = new HashMap<URL, byte[]>();
        pathname2resources = new HashMap<String, List<URL>>();

        List<String> classPathDirs = parseClassPath(classPath);

        ZipInputStream zis = new ZipInputStream(zipFile);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.isDirectory())
                continue;
            String resourceName = ze.getName();
            byte[] b = getByteArrayFromZip(zis);

            for (String cpDir : classPathDirs) {
                if(resourceName.startsWith(cpDir)) {
                    int toRemove;
                    if(!(cpDir.endsWith(".jar") || cpDir.endsWith(".class"))) {
                        toRemove = cpDir.length();
                    } else {
                        toRemove = 0;
                    }
                    if(!resourceName.endsWith(".jar")) {
                        URL targetURL = new URL("zip", "/", resourceName.substring(toRemove));
                        storeUrlResource(targetURL, b);
                    } else {
                        InputStream is = new ByteArrayInputStream(b);
                        initJarFile(resourceName.substring(toRemove), is);
                    }
                }
            }
        }
    }

    private List<String> parseClassPath (final String classPath) {
        StringTokenizer tokenizer = new StringTokenizer(classPath,":");
        List<String> patternList = new ArrayList<String>(tokenizer.countTokens());
        while(tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if(!token.endsWith("/") && !token.endsWith(".jar") && !token.endsWith(".class"))
                token = token.concat("/");
            patternList.add(token);
        }
        return patternList;
    }

    private void storeUrlResource (final URL targetURL, final byte[] b) {
        if (!pathname2resources.containsKey(targetURL.getFile())) {
            List<URL> newEntry = new ArrayList<URL>();
            newEntry.add(targetURL);
            pathname2resources.put(targetURL.getFile(), newEntry);
        } else {
            pathname2resources.get(targetURL.getFile()).add(targetURL);
        }
        resourceStore.put(targetURL, b);
    }

    public InputStream getResourceAsStreamFromURL (URL input) {
        byte[] b = resourceStore.get(input);
        return new ByteArrayInputStream(b);
    }

    @Override
    public InputStream getResourceAsStream (String name) {
        if (pathname2resources.get(name) == null)
            return super.getResourceAsStream(name);
        return getResourceAsStreamFromURL(pathname2resources.get(name).get(0));
    }

    @Override
    protected URL findResource (final String name) {
        if (pathname2resources.get(name) == null)
            return super.findResource(name);
        return pathname2resources.get(name).get(0);
    }

    @Override
    protected Enumeration<URL> findResources (final String name) throws IOException {

        final List<URL> resources = pathname2resources.get(name);
        if (resources != null) {
            return new Enumeration<URL>() {

                private Iterator<URL> _iterator = resources.iterator();

                @Override
                public boolean hasMoreElements () {
                    return _iterator.hasNext();
                }

                @Override
                public URL nextElement () {
                    return _iterator.next();
                }
            };
        } else
            return super.findResources(name);
    }

    @Override
    protected Class<?> findClass (String name) throws ClassNotFoundException {

        String resourceName = name.replace('.', '/') + ".class";
        List<URL> urls = pathname2resources.get(resourceName);
        if (urls == null)
            throw new ClassNotFoundException(name);
        byte[] b = resourceStore.get(urls.get(0));

        Class<?> clazz = defineClass(name, b, 0, b.length);
        return clazz;
    }

    private byte[] getByteArrayFromZip (ZipInputStream jis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        for (int len = 0; len != -1; ) {
            len = jis.read(b);
            if (len != -1)
                baos.write(b, 0, len);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    private void initJarFile (String jarName, InputStream is) throws IOException {
        JarInputStream jis = new JarInputStream(is);

        JarEntry je;
        while ((je = jis.getNextJarEntry()) != null) {
            if (!je.isDirectory()) {
                String resourceName = je.getName();
                byte[] b = getByteArrayFromZip(jis);
                storeUrlResource(new URL("zip", jarName + "/", resourceName), b);
            }
        }
    }

}
