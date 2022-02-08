package upgma;

import java.io.File;

public class Tools {

    public static void renameTo(String oldfile, String newfile) {
        // File (or directory) with old name
        File file = new File(oldfile);

        // File (or directory) with new name
        File file2 = new File(newfile);

        // Rename file (or directory)
        boolean success = file.renameTo(file2);
        if (!success) {
            System.out.println("problem renaming " + oldfile + " to " + newfile);

            System.exit(1);
        }



    }

    public static void delete(String fileName) {
        try {
            // Construct a File object for the file to be deleted.
            File target = new File(fileName);

            if (!target.exists()) {
                return;
            }

            // Quick, now, delete it immediately:
            if (target.delete()) {
            } else {
                System.err.println("Failed to delete " + fileName);
            }
        } catch (SecurityException e) {
            System.err.println("Unable to delete " + fileName + "("
                    + e.getMessage() + ")");
        }
    }
}
