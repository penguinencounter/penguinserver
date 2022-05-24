package org.penguinencounter.penguinserver.fplib;

public final class SkinLayers {
    public boolean cape;
    public boolean jacket;
    public boolean lSleeve;
    public boolean rSleeve;
    public boolean lPants;
    public boolean rPants;
    public boolean hat;

    public SkinLayers(
            boolean cape,
            boolean jacket,
            boolean lSleeve,
            boolean rSleeve,
            boolean lPants,
            boolean rPants,
            boolean hat
    ) {
        this.cape = cape;
        this.jacket = jacket;
        this.lSleeve = lSleeve;
        this.rSleeve = rSleeve;
        this.lPants = lPants;
        this.rPants = rPants;
        this.hat = hat;
    }

    public SkinLayers copy() {
        return new SkinLayers(
                this.cape,
                this.jacket,
                this.lSleeve,
                this.rSleeve,
                this.lPants,
                this.rPants,
                this.hat
        );
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SkinLayers sl) {
            return (
                    this.cape == sl.cape &&
                    this.jacket == sl.jacket &&
                    this.lSleeve == sl.lSleeve &&
                    this.rSleeve == sl.rSleeve &&
                    this.lPants == sl.lPants &&
                    this.rPants == sl.rPants &&
                    this.hat == sl.hat
            );
        } else return false;
    }
}
