package io.github.jake404notfound.simple_block_physics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(),
            "simpleblockphysics.json");

    public static void init() {
        ConfigData data;
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                data = GSON.fromJson(reader, ConfigData.class);
            } catch (IOException e) {
                System.err.println("Failed to read Simple Block Physics config");
                e.printStackTrace();
                data = new ConfigData();
            }
        } else {
            data = new ConfigData();
            save(data);
        }
        Config.load(data);
    }

    private static void save(ConfigData data) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Failed to save Simple Block Physics config");
            e.printStackTrace();
        }
    }
}
