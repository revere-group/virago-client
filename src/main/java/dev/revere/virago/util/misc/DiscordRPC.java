package dev.revere.virago.util.misc;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import dev.revere.virago.Virago;
import dev.revere.virago.util.Logger;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/3/2024
 */
@Setter
@Getter
public class DiscordRPC {
    private boolean running;
    private boolean canLoad;
    private Activity activity;
    private Core core;

    public DiscordRPC() {
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                File discordLibrary = downloadNativeLibrary();
                if (discordLibrary == null) {
                    System.err.println("Failed to download Discord SDK.");
                    System.exit(-1);
                }
                Core.init(discordLibrary);
                canLoad = true;

                start();
            } catch (Exception e) {
                Logger.err("Failed to load Discord SDK: " + e.getMessage(), getClass());
            }
        }
    }

    public static File downloadNativeLibrary() throws IOException {
        String name = "discord_game_sdk";
        String suffix;

        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (osName.contains("windows")) {
            suffix = ".dll";
        } else if (osName.contains("linux")) {
            suffix = ".so";
        } else if (osName.contains("mac os")) {
            suffix = ".dylib";
        } else {
            throw new RuntimeException("Cannot determine OS type: " + osName);
        }

        if (arch.equals("amd64"))
            arch = "x86_64";

        String zipPath = "lib/" + arch + "/" + name + suffix;

        URL downloadUrl = new URL("https://cdn.discordapp.com/attachments/1018898358385000469/1230595944232390828/discord_game_sdk.zip?ex=6633e4c8&is=66216fc8&hm=effc1bd09c86257a818f9228f0010790b377407bfc370f4ee7ec805a4bd2374a&");

        HttpsURLConnection connection = (HttpsURLConnection) downloadUrl.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        try (ZipInputStream zin = new ZipInputStream(connection.getInputStream())) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(zipPath)) {
                    File tempDir = Files.createTempDirectory("java-" + name + System.nanoTime()).toFile();
                    tempDir.deleteOnExit();

                    File temp = new File(tempDir, name + suffix);
                    temp.deleteOnExit();

                    Files.copy(zin, temp.toPath());

                    zin.closeEntry();

                    return temp;
                }
                zin.closeEntry();
            }
        }

        return null;
    }

    public void start() {
        if (!canLoad || running) return;
        running = true;

        try {
            CreateParams params = new CreateParams();
            params.setClientID(780772474333429761L);
            params.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD);

            core = new Core(params);

            activity = new Activity();

            activity.setDetails(Virago.getInstance().getName() + " v" + Virago.getInstance().getVersion());

            activity.timestamps().setStart(Instant.now());

            activity.assets().setLargeImage("logo");
            activity.assets().setLargeText("Virago " + Virago.getInstance().getVersion() + " @ virago.dev");

            core.activityManager().updateActivity(activity);

            new Thread(() -> {
                while (running) {
                    try {
                        ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
                        if (serverData != null) {
                            activity.setState("Playing on " + serverData.serverIP);
                        } else if (Minecraft.getMinecraft().isSingleplayer()) {
                            activity.setState("In Singleplayer");
                        } else {
                            activity.setState("Currently Idle");
                        }
                        core.activityManager().updateActivity(activity);
                        core.runCallbacks();
                        Thread.sleep(20);
                    } catch (Exception e) {
                        Logger.err("Failed to update Discord RPC: " + e.getMessage(), getClass());
                    }
                }
            }, "Discord RPC").start();
        } catch (Exception e) {
            Logger.err("Failed to start Discord RPC: " + e.getMessage(), getClass());
            running = false;
            canLoad = false;
        }
    }
}
