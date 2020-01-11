package configuration;

import controller.Mode;
import controller.Modulation;

public class Configuration {
    private Mode mode;
    private Modulation modulation;
    private Boolean coded;

    public Configuration() {
    }

    public Configuration(Mode mode, Modulation modulation, Boolean coded) {
        this.mode = mode;
        this.modulation = modulation;
        this.coded = coded;
    }

    public Mode getMode() {
        return mode;
    }

    public Modulation getModulation() {
        return modulation;
    }

    public Boolean getCoded() {
        return coded;
    }
}
