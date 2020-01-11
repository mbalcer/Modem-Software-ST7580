package configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class SaveConfiguration {
    private File configFile;

    public SaveConfiguration(File file) {
        this.configFile = file;
    }

    public boolean saveConfiguration(Configuration config) {

        try {
            Properties props = new Properties();
            props.setProperty("mode", config.getMode().toString());
            props.setProperty("modulation", config.getModulation().toString());
            props.setProperty("coded", config.getCoded().toString());

            FileWriter writer = new FileWriter(configFile);
            props.store(writer, "Configuration save");
            writer.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            return false;
        } catch (IOException ex) {
            return false;
        }

        return true;
    }
}
