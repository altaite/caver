package upgma;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public abstract class UPGMA {

    protected List<Item> items = new ArrayList<Item>();
    protected List<Cluster> clusters = new ArrayList<Cluster>();
    protected String edgesFile;

    public UPGMA() {
        this.edgesFile = "<not specified>";
    }

    public UPGMA(String edgesFile) {
        this.edgesFile = edgesFile;
    }

    public void addItem(Item i) {
        this.items.add(i);
    }

    public abstract Cluster cluster();

    public String getStringHierarchy(Cluster root, int level, BufferedWriter bw)
            throws IOException {
        boolean printItems = false;

        String childMergeDist = "";
        if (root.hasChildren()) {
            childMergeDist = "" + root.getMergeDist() + "";
        }
        if (root.child1 != null && root.child2 != null) {
            bw.write("  " + (root.child1.getId() + 1) + " "
                    + (printItems ? root.child1.getItemIDS() : "") + "");
            bw.write("  " + (root.child2.getId() + 1) + " "
                    + (printItems ? root.child2.getItemIDS() : "") + "");
        }

        if (root.child1 != null && root.child2 != null) {
            bw.write((printItems ? root.getItemIDS() : "") + " "
                    + childMergeDist + " " + (root.getId() + 1));
            bw.write("\n");
        }

        if (root.hasChildren()) {
            getStringHierarchy(root.child1, level + 1, bw);
            getStringHierarchy(root.child2, level + 1, bw);
        }
        return "";
    }

    public void getStringHierarchyPlain(Cluster root, BufferedWriter bw)
            throws IOException {

        Queue<Cluster> q = new PriorityQueue<Cluster>(100,
                new Comparator<Cluster>() {

                    @Override
                    public int compare(Cluster o1, Cluster o2) {
                        return o2.getId() - o1.getId();
                    }
                });


        // used for reordering top-down to bottom-up
        // to keep the format
        List<String> lines = new ArrayList<String>();

        q.add(root);
        while (!q.isEmpty()) {
            Cluster best = q.remove();


            if (best.hasChildren()) {
                lines.add("" + (best.child1.id + 1) + "\t"
                        + (best.child2.id + 1) + "\t" + best.getMergeDist()
                        + "\t" + (best.id + 1) + "\n");
                q.add(best.child1);
                q.add(best.child2);
            }
        }

        // all done, reverse the list and print it
        Collections.reverse(lines);
        for (String s : lines) {
            bw.write(s);
        }

    }

    public static boolean hierarchiesEqual(Cluster c1, Cluster c2) {
        boolean root = c1.getId() == c2.getId();

        if (!c1.hasChildren() && !c2.hasChildren()) {
            return root;
        }

        boolean firstValid = false;

        Cluster cx = c1.child1;
        Cluster cy1 = c2.child1;
        Cluster cy2 = c2.child2;

        if (cx != null && cy1 != null) {
            firstValid = firstValid || hierarchiesEqual(cx, cy1);
        }
        if (cx != null && cy2 != null) {
            firstValid = firstValid || hierarchiesEqual(cx, cy2);
        }

        boolean secondValid = false;
        cx = c1.child2;
        cy1 = c2.child1;
        cy2 = c2.child2;

        if (cx != null && cy1 != null) {
            secondValid = secondValid || hierarchiesEqual(cx, cy1);
        }
        if (cx != null && cy2 != null) {
            secondValid = secondValid || hierarchiesEqual(cx, cy2);
        }

        boolean result = true;
        result = result && firstValid;
        result = result && secondValid;

        return root && result;

    }
}
