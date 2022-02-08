package upgma;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EdgeLoader {

    // load edges from file .edges which has "i j  value" format
    // internally switched to 0-based !
    public static List<Item> loadItems(String filename) {

        //WARNING: TreeSet, replaced with hashSet, order not required. if yes, 
        // sort should be done at the end of this method
        Set<Integer> clusterIds = new HashSet<Integer>();
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader input = new BufferedReader(fr);
            String line;
            while ((line = input.readLine()) != null) {
                if (line.length() > 6) {
                    String[] values = line.trim().split("\\s+");
                    if (values.length > 2) {
                        if (!values[0].equalsIgnoreCase("")) {
                            clusterIds.add(Integer.parseInt(values[0]));
                        }
                    }
                    if (values.length > 2) {
                        if (!values[1].equalsIgnoreCase("")) {
                            clusterIds.add(Integer.parseInt(values[1]));
                        }
                    }
                }
            }

            fr.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EdgeLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EdgeLoader.class.getName()).log(Level.SEVERE, null, ex);
        }



        List<Item> items = new ArrayList<Item>();
        for (int i : clusterIds) {
            items.add(new Item(i - 1));
        }


        return items;
    }

    // 0-based expected!
    public static List<Item> loadItemsBinary(String filename) {

        //WARNING: TreeSet, replaced with hashSet, order not required. 
        // if yes, sort should be done at the end of this method
        Set<Integer> clusterIds = new HashSet<Integer>();



        try {
            DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(filename), 1024 * 1024));

            while (true) {
                int c1 = is.readInt();
                int c2 = is.readInt();
                float value = is.readFloat();
                clusterIds.add(c1);
                clusterIds.add(c2);
            }
        } catch (EOFException eof) {
            // DO NOTHING, null returned
        } catch (IOException ex) {
            Logger.getLogger(DiskSupport.class.getName()).log(Level.SEVERE, null, ex);
        }



        List<Item> items = new ArrayList<Item>();
        for (int i : clusterIds) {
            items.add(new Item(i - 1));
        }


        return items;
    }

    // load edges from file .edges which has "i j  value" format
    // internally switched to 0-based !
    // used by NAIVE UPGMA
    public static List<Edge> loadEdges(String filename) {

        List<Edge> edges = new ArrayList<Edge>();

        try {
            FileReader fr = new FileReader(filename);
            BufferedReader input = new BufferedReader(fr);
            String line;
            while ((line = input.readLine()) != null) {
                if (line.length() > 3) {
                    String[] values = line.trim().split("\\s+");
                    if (values.length > 2) {
                        Edge e = new Edge(Integer.parseInt(values[0]) - 1,
                                Integer.parseInt(values[1]) - 1,
                                Float.parseFloat(values[2]));
                        edges.add(e);
                    }

                }
            }
            fr.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EdgeLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EdgeLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return edges;
    }
}
