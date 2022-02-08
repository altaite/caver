package caver.ui.animation;

import geometry.primitives.Sphere;
import java.util.List;

/*
 * Represents a chain of spheres in PyMOL, i.e. a tunnel in a specific snapshot
 * and with assigned PDB file identifiers chain, resi and segi.
 */
public class Chain {

    private String segi;
    private String resi;
    private char chain;
    private List<Sphere> spheres;

    public Chain(List<Sphere> spheres, char chain, String segi, String resi) {
        this.spheres = spheres;
        this.chain = chain;
        this.segi = segi;
        this.resi = resi;

    }

    public Sphere get(int i) {
        return spheres.get(i);
    }

    public int size() {
        return spheres.size();
    }

    public Sphere first() {
        return spheres.get(0);
    }

    public String getResi() {
        return resi;
    }

    public String getSegi() {
        return segi;
    }

    public char getChain() {
        return chain;
    }
}
