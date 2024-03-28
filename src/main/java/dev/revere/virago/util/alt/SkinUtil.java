package dev.revere.virago.util.alt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Remi
 * @project Virago
 * @date 3/27/2024
 */
public class SkinUtil {

    private static final Map<String, ResourceLocation> SKIN_CACHE = new HashMap<>();

    public static ResourceLocation getResourceLocation(SkinType skinType, String uuid, int size) {
        if (SKIN_CACHE.containsKey(uuid)) return SKIN_CACHE.get(uuid);

        String imageUrl = "http://crafatar.com/avatars/" + uuid;
        ResourceLocation resourceLocation = new ResourceLocation("skins/" + uuid + "?overlay=true");
        ThreadDownloadImageData headTexture = new ThreadDownloadImageData(null, imageUrl, null, null);
        Minecraft.getMinecraft().getTextureManager().loadTexture(resourceLocation, headTexture);
        SKIN_CACHE.put(uuid, resourceLocation);
        AbstractClientPlayer.getDownloadImageSkin(resourceLocation, uuid);
        return resourceLocation;
    }

    public static UUID getUUIDFromName(String playerName) throws IOException {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + playerName;

        URL apiUrl = new URL(url);

        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        reader.close();

        // Close the connection
        connection.disconnect();

        // Parse the JSON response
        String response = responseBuilder.toString();
        String uuidString = response.substring(response.indexOf("\"id\":\"") + 6, response.indexOf("\",\"name\":"));

        // Convert the UUID string to UUID object
        UUID uuid = UUID.fromString(uuidString.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        ));

        return uuid;
    }

    public enum SkinType {
        AVATAR, HELM, BUST, ARMOR_BUST, BODY, ARMOR_BODY, CUBE, SKIN
    }
}
