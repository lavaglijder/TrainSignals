package nl.ijsglijder.traincraft.files;

import nl.ijsglijder.traincraft.TrainCraft;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class TrainFile {

    private String path;
    private File file;
    private FileConfiguration fc;

    public TrainFile(String path, boolean configFile) {
        this.path = path;

        this.file = new File(getPlugin().getDataFolder(), path);

        if(!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            getPlugin().saveResource(path, configFile);
        }

        this.fc = new YamlConfiguration();
        try {
            this.fc.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            this.fc.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.fc.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getFileConfig() {
        return this.fc;
    }

    private JavaPlugin getPlugin() {
        return TrainCraft.getPlugin(TrainCraft.class);
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getFc() {
        return fc;
    }
}
