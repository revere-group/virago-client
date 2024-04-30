import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

import net.minecraft.client.main.Main;

public class Start
{
    public static void main(String[] args)
    {
        Main.main(concat(new String[] {"--version", "mcp", "--accessToken", "0", "--assetsDir", new File(getMinecraftDirectory(), "assets").getAbsolutePath(), "--assetIndex", "1.8", "--userProperties", "{}"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second)
    {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static File getMinecraftDirectory() {
        String userHome = System.getProperty("user.home", ".");
        String os = System.getProperty("os.name").toLowerCase();
        String minecraftDir;

        if (os.contains("win")) {
            minecraftDir = userHome + "\\AppData\\Roaming\\.minecraft";
        } else if (os.contains("mac")) {
            minecraftDir = userHome + "/Library/Application Support/minecraft";
        } else {
            minecraftDir = userHome + "/.minecraft";
        }

        return new File(minecraftDir);
    }
}