package caver.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Collection of methods originally developed for HotSpot Wizard
 * (http://loschmidt.chemi.muni.cz/hotspotwizard).
 */
public class HotSpotWizardMyUtil {

    public static BufferedReader getBufferedReader(InputStream is) {
        DataInputStream dis = new DataInputStream(is);
        InputStreamReader isr = new InputStreamReader(dis);
        BufferedReader br = new BufferedReader(isr);
        return br;
    }

    /*
     * Prepares @see BufferedReader for download from url @param resource.
     */
    public static BufferedReader getBufferedReaderFromResource(String resource)
            throws IOException {
        try {
            HttpURLConnection con =
                    (HttpURLConnection) new URL(resource).openConnection();
            con.connect();
            InputStream is = con.getInputStream();
            BufferedReader br = HotSpotWizardMyUtil.getBufferedReader(is);
            return br;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static File downloadFile(String address, String fileName)
            throws IOException {
        return downloadFile(address, new File(fileName));
    }

    /**
     * Downloads file by HTTP protocol.
     *
     * @param address
     * @param localFileName
     */
    public static File downloadFile(String address, File file)
            throws IOException {
        try {
            OutputStream out = null;
            URLConnection conn = null;
            InputStream in = null;
            file.createNewFile();
            try {
                URL url = new URL(address);
                out = new BufferedOutputStream(
                        new FileOutputStream(file));
                conn = url.openConnection();
                in = conn.getInputStream();
                byte[] buffer = new byte[1024];
                int numRead;
                while ((numRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, numRead);
                }
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ioe) {
                    Logger.getLogger("caver").log(Level.WARNING, "", ioe);
                }
            }
            return file;
        } catch (IOException e) {
            throw new IOException("Address: " + address + ", file: "
                    + file.getPath(), e);
        }
    }

    public static File downloadFile(InputStream in, File file)
            throws IOException {
        try {
            OutputStream out = null;

            file.createNewFile();
            try {
                byte[] buffer = new byte[1024];
                int numRead;
                while ((numRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, numRead);
                }
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ioe) {
                    Logger.getLogger("caver").log(Level.WARNING, "", ioe);
                }
            }
            return file;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Downloads file by HTTP protocol.
     *
     * @param address
     * @param localFileName
     * @param file will be created, if exists, will be overwritten.
     */
    public static File saveFile(InputStream in, File file) throws IOException {
        OutputStream out = null;
        file.createNewFile();
        try {
            out = new BufferedOutputStream(
                    new FileOutputStream(file));
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                Logger.getLogger("caver").log(Level.WARNING, "", ioe);
            }
        }
        return file;
    }

    public static void copyFile(File in, File out) {
        try {
            FileChannel sourceChannel = new FileInputStream(in).getChannel();
            FileChannel destinationChannel = new FileOutputStream(out).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
            sourceChannel.close();
            destinationChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void inputStreamToFile(InputStream is, File f) {
        try {
            OutputStream out = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            is.close();
        } catch (IOException e) {
            Logger.getLogger("caver").log(Level.WARNING, "", e);
        }
    }

    public static boolean deleteDir(File fDir) {
        String[] strChildren = null;
        boolean bRet = false;

        if (fDir.isDirectory()) {
            strChildren = fDir.list();
            for (int i = 0; i < strChildren.length; i++) {
                bRet = deleteDir(new File(fDir, strChildren[i]));
                if (!bRet) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return fDir.delete();
    }

    /*
     * Returns "none" if c.size() == 0. Returns list of object.toString()
     * separated by ", " otherwise. Intended usage is generation of PyMOL
     * selection lists.
     */
    public static String collectionToString(Collection c, String separator) {
        if (c.isEmpty()) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            sb.append(o.toString());
            if (it.hasNext()) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static String collectionToPythonList(Collection c) {
        if (c.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            sb.append("'");
            sb.append(o.toString());
            sb.append("'");
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /*
     * Should be used for standard, non-debug, program output. May contain
     * logging.
     */
    public static void copyDirectory(File srcPath, File dstPath) {
        try {
            if (srcPath.isDirectory()) {
                if (!dstPath.exists()) {
                    dstPath.mkdir();
                }
                String files[] = srcPath.list();
                for (int i = 0; i < files.length; i++) {
                    copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
                }
            } else {
                if (!srcPath.exists()) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "File or directory does not exist.");
                    System.exit(0);
                } else {
                    InputStream in = new FileInputStream(srcPath);
                    OutputStream out = new FileOutputStream(dstPath);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Returns actual value of serialVersionUID for Protein class and classes it
     * aggregates. Makes serialization immune to minor changes and thus enables
     * testing against old serialized objects.
     */
    public static long getVersion() {
        return 1000;
    }

    public static String stackTraceToString(Throwable t) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        t.printStackTrace(ps);
        return bos.toString();
    }

    public static String getTime(long milliseconds) {
        Date date = new Date(milliseconds);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND) + " "
                + calendar.get(Calendar.DAY_OF_MONTH) + ". "
                + (calendar.get(Calendar.MONTH) + 1) + ". "
                + calendar.get(Calendar.YEAR);
        return time;
    }
}
