package chemistry;

class ElementImpl implements ChemicalElement {

    private String symbol_; // e.g. He
    private int z_; // proton number
    private String name_; // e.g. Helium
    private Double vdw_; // Van der Waals radius

    public ElementImpl(int z, String symbol, String name, Double vanDerWaalsRadius) {
        z_ = z;
        symbol_ = PeriodicTable.formatCase(symbol);
        name_ = name;
        vdw_ = vanDerWaalsRadius;
        if (null != vdw_ && (vdw_ < 1 || 5 < vdw_)) {
            throw new RuntimeException("Suspicious VDW radius " + vdw_);
        }
    }

    @Override
    public String getSymbol() {
        return symbol_;
    }

    @Override
    public int getProtonNumber() {
        return z_;
    }

    @Override
    public String getName() {
        return name_;
    }

    @Override
    public Double getVanDerWaalsRadius() {
        return vdw_;
    }

    @Override
    public boolean isHydrogen() {
        return "H".equals(symbol_);
    }
}
