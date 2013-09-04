package boot.zip;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import boot.ZipStarter;

public class Handler extends URLStreamHandler {

    public final static String HANDLER_KEY = "java.protocol.handler.pkgs";
    private static ZipStarter zipStarter;

    /**
     * register package as URL Handler package
     * must be called before first use of these urls
     * Note: invoked via reflection in ZipStarter
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void registerHandler (ZipStarter classLoader) {

        Handler.zipStarter = classLoader;

        synchronized (System.getProperties()) {
            String pkg = "boot";
            if (System.getProperties().contains(HANDLER_KEY)) {
                String current = System.getProperty(HANDLER_KEY);
                if (!current.contains(pkg)) {
                    System.setProperty(HANDLER_KEY, current + "|" + pkg);
                }
            } else {
                System.setProperty(HANDLER_KEY, pkg);
            }
        }
    }

    /**
     * urls may be of the following syntax:
     * zip://classpath/file-path-following-zip-classpath-syntax (be inside lib/** of the zip file)
     *
     * @param url to resolve
     * @return URLConnection to the resource
     * @throws java.io.IOException
     */
    @Override
    protected URLConnection openConnection (final URL url) throws IOException {
        return new URLConnection(url) {
            @Override
            public void connect () throws IOException {}

            @Override
            public InputStream getInputStream () throws IOException {
                return zipStarter.getResourceAsStreamFromURL(url);
            }
        };
    }
}
