package chemistry;

public interface ChemicalElement {

    public String getSymbol();

    public int getProtonNumber();

    public String getName();

    /*
     * May return null if radius does not exist for the element.
     */
    public Double getVanDerWaalsRadius();

    public boolean isHydrogen();
}
