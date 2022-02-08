package caver.util;

import java.io.File;

public class FileOperations {

    public static boolean deleteDirectory(File f) {
        if (f.isDirectory()) {
            String[] strChildren = f.list();
            for (int i = 0; i < strChildren.length; i++) {
                boolean b = deleteDirectory(new File(f, strChildren[i]));
                if (!b) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return f.delete();
    }
}
