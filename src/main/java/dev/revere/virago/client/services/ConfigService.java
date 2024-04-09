package dev.revere.virago.client.services;

import dev.revere.virago.Virago;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.util.misc.AES256;
import dev.revere.virago.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/6/2024
 */
@Getter
@Setter
public class ConfigService implements IService {

    private final HashMap<String, File> directories = new HashMap(){{
        put("virago", new File("virago"));
        put("configs", new File("virago/configs"));
        put("altmanager", new File("virago/altmanager"));
    }};;

    private File altDataFile;

    @Override
    public void initService() {
        directories.forEach((identifier, file) -> {
            if (!file.exists()) {
                file.mkdirs();
            }
        });
        altDataFile = new File(directories.get("altmanager"), "alts.txt");
        if (!altDataFile.exists()) {
            try {
                altDataFile.createNewFile();
            } catch (IOException e) {
                Logger.err("Failed to create alt data file.", getClass());
            }
        }
    }

    /**
     * Loads a config from a file.
     *
     * @param name the name
     */
    public void loadConfig(String name) {
        new Thread(() -> {
            Logger.info("Loading config " + name + "...", getClass());

            File data = new File(directories.get("configs"), name + ".json");
            if (!data.exists()) {
                Logger.err("Config " + name + " does not exist!", getClass());
                return;
            }

            JSONObject jsonData = loadJSON(data);
            try {
                Virago.getInstance().getServiceManager().getService(ModuleService.class).getModuleList().forEach(module -> {
                    try {
                        assert jsonData != null;
                        JSONObject moduleData = jsonData.getJSONObject(module.getName());

                        for(Setting setting : module.getSettingHierarchy()) {
                            if (setting.getValue() instanceof Boolean) {
                                ((Setting<Boolean>) setting).setValue(moduleData.getBoolean(setting.getPath()));
                            } else if (setting.getValue() instanceof Double) {
                                ((Setting<Double>) setting).setValue(moduleData.getDouble(setting.getPath()));
                            } else if (setting.getValue() instanceof Enum<?>) {
                                try {
                                    Enum<?> enumuration = (Enum<?>) setting.getValue();
                                    Enum<?> value = Enum.valueOf(enumuration.getClass(), moduleData.getString(setting.getPath()));

                                    ((Setting<Enum<?>>) setting).setValue(value);
                                } catch (IllegalArgumentException exception) {
                                    Logger.addChatMessage("A setting for " + module.getName() + " couldn't be loaded.");
                                }
                            } else if (setting.getValue() instanceof Color) {
                                String[] values = moduleData.getString(setting.getPath()).split(":");

                                ((Setting<Color>) setting).setValue(new Color(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2])));
                            }
                        }

                        module.setEnabledSilent(moduleData.getBoolean("enabled"));
                        module.setKey(moduleData.getInt("keybind"));
                    } catch (JSONException e) {
                        Logger.err("Failed to load module " + module.getName() + "!" + " | " + e.getMessage(), getClass());
                    }
                });

                Virago.getInstance().getServiceManager().getService(DraggableService.class).getDraggableList().forEach(draggable -> {
                    try {
                        assert jsonData != null;
                        JSONObject draggableData = jsonData.getJSONObject(draggable.getName() + "Drag");

                        draggable.setX(draggableData.getFloat("x"));
                        draggable.setY(draggableData.getFloat("y"));
                    } catch (JSONException e) {
                        Logger.err("Failed to load draggable " + draggable.getName() + "!", getClass());
                    }
                });
            } catch (Exception e) {
                Logger.err("Failed to load config " + name + "!", getClass());
            }
        }).start();
    }


    /**
     * Saves a config to a file.
     *
     * @param name the name
     */
    public void saveConfig(String name) {
        new Thread(() -> {
            Map<String, JSONObject> moduleData = new HashMap<>();
            Virago.getInstance().getServiceManager().getService(ModuleService.class).getModuleList().forEach(module -> {
                JSONObject object = new JSONObject();
                try {
                    object.put("enabled", module.isEnabled());
                    object.put("keybind", module.getKey());
                } catch (JSONException e) {
                    Logger.err("Failed to save module " + module.getName() + "!", getClass());
                }

                module.getSettingHierarchy().forEach(setting -> {
                    try {
                        if (setting.getValue() instanceof Color) {
                            object.put(setting.getPath(), ((Color) setting.getValue()).getRed() + ":" + ((Color) setting.getValue()).getGreen() + ":" + ((Color) setting.getValue()).getBlue());
                        } else if (setting.getValue() instanceof Enum<?>) {
                            object.put(setting.getPath(), ((Enum<?>) setting.getValue()).name());
                        } else {
                            object.put(setting.getPath(), setting.getValue());
                        }
                    } catch (JSONException exception) {
                        Logger.err("Failed to save setting " + setting.getPath() + " for module " + module.getName() + "!", getClass());
                    }
                });

                moduleData.put(module.getName(), object);
            });

            Virago.getInstance().getServiceManager().getService(DraggableService.class).getDraggableList().forEach(draggable -> {
                JSONObject object = new JSONObject();
                try {
                    object.put("x", draggable.getX());
                    object.put("y", draggable.getY());
                } catch (JSONException e) {
                    Logger.err("Failed to save draggable " + draggable.getName() + "!", getClass());
                }

                moduleData.put(draggable.getName() + "Drag", object);
            });

            saveObject(new File(directories.get("configs"), name + ".json"), moduleData);
        }).start();
    }

    /**
     * Saves a JSON object to a file.
     *
     * @param file the file
     * @param objects the JSON objects
     */
    private void saveObject(File file, Map<String, JSONObject> objects) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            JSONObject object = new JSONObject();
            objects.forEach((name, json) -> {
                try {
                    object.put(name, json);
                } catch (JSONException e) {
                    Logger.err("Failed to save object " + name + "!", getClass());
                }
            });

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(object.toString(4));
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException | JSONException exception) {
            Logger.err("Failed to save file!", getClass());
        }
    }

    /**
     * Deletes a config.
     *
     * @param name the name
     */
    public void deleteConfig(String name) {
        File file = new File(directories.get("configs"), name + ".json");
        if (file.exists()) {
            if (file.delete()) {
                Logger.info("Deleted config " + name + "!", getClass());
            } else {
                Logger.err("Failed to delete config " + name + "!", getClass());
            }
        } else {
            Logger.err("Config " + name + " does not exist!", getClass());
        }
    }

    /**
     * Saves the license key to a file.
     *
     * @param key the license key
     */
    public void saveLicenseKey(String key) {
        File file = new File(directories.get("virago"), "license.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(key);
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException exception) {
            Logger.err("Failed to save license key!", getClass());
        }
    }

    /**
     * Loads the license key from a file.
     *
     * @return the license key
     */
    public String loadLicenseKey() {
        String result = "";
        File file = new File(directories.get("virago"), "license.txt");
        try {
            if (!file.exists()) return result;

            result = FileUtils.readFileToString(file);

            return result;
        } catch (IOException exception) {
            Logger.err("Failed to load license key!", getClass());
        }
        return result;
    }

    /**
     * Loads a JSON file.
     *
     * @param file the file
     * @return the JSON object
     */
    private JSONObject loadJSON(File file) {
        try {
            return new JSONObject(FileUtils.readFileToString(file));
        } catch (JSONException | IOException e) {
            return null;
        }
    }

    public File[] listConfigs() {
        File configsDirectory = directories.get("configs");
        if (configsDirectory != null && configsDirectory.exists() && configsDirectory.isDirectory()) {
            return configsDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        } else {
            Logger.err("Configs directory does not exist or is not a directory.", getClass());
            return null;
        }
    }

    /**
     * Checks if a config exists.
     *
     * @param name the name
     * @return if the config exists
     */
    public boolean configExists(String name) {
        return new File(directories.get("configs"), name + ".json").exists();
    }
}
