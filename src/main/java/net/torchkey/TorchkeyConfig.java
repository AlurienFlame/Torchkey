package net.torchkey;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.fabricmc.loader.api.FabricLoader;

public class TorchkeyConfig {
  JsonObject config = new JsonObject();
  static String configFilename = "torchkey.json";
  static Path configDir = FabricLoader.getInstance().getConfigDir();
  static Path configPath = configDir.resolve(configFilename);
  static File configFile = configPath.toFile();
  static String defaultConfig = "{\"validTorches\": [\"torch\", \"redstone_torch\", \"stone_torch\", \"soul_torch\"]}";

  public TorchkeyConfig() {
    if (!Files.exists(configPath)) {
      create();
      config = new Gson().fromJson(defaultConfig, new JsonObject().getClass());
      write();
    }
    read();
  }

  private void create() {
    try {
      configFile.getParentFile().mkdirs();
      Files.createFile(configPath);
    } catch (Exception e) {
      System.out.println(String.format("[Torchkey] Error creating config file: %s", e.toString()));
    }
  }

  private void read() {
    try {
      FileReader reader = new FileReader(configFile);
      config = new Gson().fromJson(reader, JsonObject.class);
      reader.close();
    } catch (Exception e) {
      System.out.println(String.format("[Torchkey] Error reading config file: %s", e.toString()));
    }
  }

  private void write() {
    try {
      FileWriter writer = new FileWriter(configFile);
      new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
      writer.close();
    } catch (IOException e) {
      System.out.println(String.format("[Torchkey] Error saving config file: %s", e.toString()));
    }
  }

  public ArrayList<String> getOrDefault(String key, ArrayList<String> defaultValue) {
    if (config.has(key)) {
      ArrayList<String> result = new ArrayList<String>();
      for (JsonElement item : config.get(key).getAsJsonArray()) {
        result.add(item.getAsString());
      }
      return result;
    }
    return defaultValue;
  }
}
