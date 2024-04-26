package dev.revere.virago.client.services;

import dev.revere.virago.Virago;
import dev.revere.virago.api.alt.Alt;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.util.Logger;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Remi
 * @project Virago
 * @date 3/27/2024
 */
@Setter
@Getter
public class AltService implements IService {

    private final Path dataFile;
    public ArrayList<String> save = new ArrayList<>();
    public ArrayList<Alt> alts = new ArrayList<>();
    public String status;

    public AltService() {
        Path altManagerFolder = Paths.get(Virago.getInstance().getClientDir().toString(), "altmanager");
        dataFile = Paths.get(altManagerFolder.toString(), "alts.txt");
    }

    @Override
    public void initService() {
        load();
    }

    @Override
    public void stopService() {
        save();
    }

    public void save() {
        save.clear();

        for (Alt alt : alts) {
            addLine(alt);
        }

        try {
            PrintWriter printWriter = new PrintWriter(dataFile.toFile());
            for (String str : save) {
                printWriter.println(str);
            }
            printWriter.close();
        } catch (FileNotFoundException e) {
            Logger.err("Failed to save alts.", getClass());
        }
    }

    public void load() {
        alts.clear();
        ArrayList<String> lines = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataFile.toFile()));
            String line = reader.readLine();

            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            Logger.err("Failed to load alts.", getClass());
        }

        for (String string : lines) {
            String[] args = string.split(":");

            if (args[0] != null && args[1] != null && args[2] != null && args[3] != null && args[4] != null && args[5] != null) {
                try {
                    addAlt(new Alt(args[0], args[1], args[2], Long.parseLong(args[3]), args[4], args[5]));
                } catch (Exception ignored) {

                }
            }
        }
    }

    /**
     * Adds a line to the save list
     *
     * @param alt the alt to add
     */
    public void addLine(Alt alt) {
        save.add(alt.getAlias() + ":" + alt.getUsername() + ":" + alt.getPassword() + ":" + alt.getCreationDate() + ":" + alt.getType() + ":" + alt.getUuid());
    }

    /**
     * Adds an alt to the alts list
     *
     * @param alt the alt to add
     */
    public void addAlt(Alt alt) {
        alts.add(alt);
    }

    /**
     * Checks if the given username is a valid cracked alt
     *
     * @param username the username to check
     * @return if the username is a valid cracked alt
     */
    public boolean isValidCrackedAlt(String username) {
        return !username.equals("") && !username.contains("&") && !username.contains("-")
                && !username.contains("+") && !username.contains("/") && !username.contains("\\") &&
                !username.contains(".") && !username.contains("@") && username.length() <= 16 && username.length() >= 3;
    }
}
