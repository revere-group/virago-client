package dev.revere.virago.util.alt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.revere.virago.util.Logger;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * @author Remi
 * @project Virago
 * @date 3/27/2024
 */
@UtilityClass
public class CookieLogin {
    private final Gson gson = new Gson();

    private final String useragent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";

    public static LoginData loginWithCookie(File cookieFile) throws Exception {
        try {
            String[] cookiesText = FileUtils.readFileToString(cookieFile).split("\n");

            StringBuilder sb = new StringBuilder();

            for (String cookie : cookiesText) {
                String name = cookie.split("\t")[5].trim();
                String value = cookie.split("\t")[6].trim();
                sb.append(name).append("=").append(value).append("; ");
            }

            String cookie = sb.toString();

            String location = getNextLocation(new URL("https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin&aid=1142970254".replaceAll(" ", "%20")), "PHPSESSID=0");
            String location2 = getNextLocation(new URL(location.replaceAll(" ", "%20")), cookie);
            String location3 = getNextLocation(new URL(location2.replaceAll(" ", "%20")), cookie);
            String accessToken = location3.split("accessToken=")[1];

            String decoded = new String(Base64.getDecoder().decode(accessToken), StandardCharsets.UTF_8).split("\"rp://api.minecraftservices.com/\",")[1];
            String token = decoded.split("\"Token\":\"")[1].split("\"")[0];
            String uhs = decoded.split(Pattern.quote("{\"DisplayClaims\":{\"xui\":[{\"uhs\":\""))[1].split("\"")[0];

            // build xbl token
            String xbl = "XBL3.0 x=" + uhs + ";" + token;

            // login with that xbl
            String output = postExternal("https://api.minecraftservices.com/authentication/login_with_xbox", "{\"identityToken\":\"" + xbl + "\",\"ensureLegacyEnabled\":true}", true);
            String mcToken = gson.fromJson(output, JsonObject.class).get("access_token").getAsString();

            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + mcToken);

            JsonObject profileResponse = gson.fromJson(IOUtils.toString(connection.getInputStream()), JsonObject.class);
            return new LoginData(mcToken, null, profileResponse.get("id").getAsString(), profileResponse.get("name").getAsString());
        } catch (Exception e) {
            Logger.err("Failed to login with cookie. Err: " + e.getMessage(), CookieLogin.class);
            return null;
        }
    }

    public static String getNextLocation(URL url, String cookie) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        connection.setRequestProperty("Accept-Encoding", "niggas");
        connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestProperty("User-Agent", useragent);
        connection.setInstanceFollowRedirects(false);
        connection.connect();

        return connection.getHeaderField("Location");
    }

    public String postExternal(final String url, final String post, final boolean json) {
        try {
            final HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", useragent);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            final byte[] out = post.getBytes(StandardCharsets.UTF_8);
            final int length = out.length;
            connection.setFixedLengthStreamingMode(length);
            connection.addRequestProperty("Content-Type", json ? "application/json" : "application/x-www-form-urlencoded; charset=UTF-8");
            connection.addRequestProperty("Accept", "application/json");
            connection.connect();
            try (final OutputStream os = connection.getOutputStream()) {
                os.write(out);
            }

            final int responseCode = connection.getResponseCode();

            final InputStream stream = responseCode / 100 == 2 || responseCode / 100 == 3 ? connection.getInputStream() : connection.getErrorStream();

            if (stream == null) {
                System.err.println(responseCode + ": " + url);
                System.out.println(IOUtils.toString(connection.getInputStream()));
                return null;
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String lineBuffer;
            final StringBuilder response = new StringBuilder();
            while ((lineBuffer = reader.readLine()) != null) {
                response.append(lineBuffer);
            }

            reader.close();

            return response.toString();
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class LoginData {
        public String mcToken;
        public String newRefreshToken;
        public String uuid, username;

        public LoginData(final String mcToken, final String newRefreshToken, final String uuid, final String username) {
            this.mcToken = mcToken;
            this.newRefreshToken = newRefreshToken;
            this.uuid = uuid;
            this.username = username;
        }
    }
}
