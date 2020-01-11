package configuration;

import controller.Mode;
import controller.Modulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class LoadConfiguration {

    private File configFile;

    public LoadConfiguration(File file) {
        this.configFile = file;
    }

    public Configuration getConfiguration() {
        Configuration configuration = null;
        try {
            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);

            Mode mode = Mode.valueOf(props.getProperty("mode"));
            Modulation modulation = Modulation.valueOf(props.getProperty("modulation"));
            Boolean coded = Boolean.valueOf(props.getProperty("coded"));

            configuration = new Configuration(mode, modulation, coded);

            reader.close();
        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
            // I/O error
        }

        return configuration;
    }
}
