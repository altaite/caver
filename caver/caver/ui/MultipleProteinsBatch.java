package caver.ui;

import caver.CalculationSettings;
import caver.Printer;
import java.io.File;
import java.io.IOException;

/*
 * Support for running a multiple Caver computations. Experimental.
 */
public class MultipleProteinsBatch {

    public static void main(String[] args)
            throws IOException, CalculationException {
        File superDir;
        if (1 <= args.length) {
            superDir = new File(args[0]);
        } else {
            superDir = new File("./test_data/test_1");
        }

        for (File dir : new File(superDir + File.separator + "data").listFiles()) {

            try {
                if (!dir.isDirectory()) {
                    continue;
                }

                CalculationSettings cs = new CalculationSettings();

                cs.setBinDirectory(new File(superDir + File.separator + "in"));
                cs.setConfigFiles(new File(superDir + File.separator
                        + "config.txt.adv"),
                        new File(superDir + File.separator + "config.txt.adv"));

                cs.readFile();

                cs.setPdbDir(new File(dir + File.separator + "pdb"));
                cs.setConfigFiles(new File(dir + File.separator + "config.txt"),
                        new File(superDir + File.separator + "config.txt.adv"));

                cs.setOutputDirectory(new File(dir + File.separator + "out"));

                cs.readFile();

                Launcher launcher = new Launcher(cs);
                launcher.run();
            } catch (SettingsException e) {
                Printer.log(e);
            }
        }
    }
}
