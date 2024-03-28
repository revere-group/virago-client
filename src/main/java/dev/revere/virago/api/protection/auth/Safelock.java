package dev.revere.virago.api.protection.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.revere.virago.Virago;
import dev.revere.virago.api.protection.ViragoUser;
import dev.revere.virago.api.protection.rank.Rank;
import dev.revere.virago.client.gui.menu.CustomGuiMainMenu;
import dev.revere.virago.client.gui.menu.GuiLicenceKey;
import lombok.var;
import net.minecraft.client.Minecraft;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * @author Remi
 * @project Virago
 * @date 3/20/2024
 */
public class Safelock {
    private String productKey;
    private String server;
    private String authorization;

    private static final String UNKNOWN = "unknown";
    private static String OS = System.getProperty("os.name").toLowerCase();

    public Safelock(String licenseKey, String validationServer, String authorization) {
        this.productKey = licenseKey;
        this.server = validationServer;
        this.authorization = authorization;
    }

    public boolean unlock() {
        String[] respo = isValid();
        if (respo[0].equals("2") && Boolean.parseBoolean(respo[3])) {
            return Boolean.parseBoolean(respo[3]);
        } else if (respo[0].equals("3") && Boolean.parseBoolean(respo[3]) && Boolean.parseBoolean(respo[3])) {
            return Boolean.parseBoolean(respo[3]);
        } else {
            return Boolean.parseBoolean(respo[3]);
        }
    }

    private String requestServer(String productKey) throws IOException {
        URL url = new URL(server);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Safelock");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        String hwid = getMac();

        String outString = "{\"hwid\":\"password\",\"licensekey\":\"avain\",\"product\":\"NiceCar\",\"version\":\"dogpoop\"}";
        //Align HWID again here if someone tries to spoof it
        outString = outString
                .replaceAll("password", getHWID())
                .replaceAll("avain", productKey)
                .replaceAll("NiceCar", Virago.getInstance().getName())
                .replaceAll("dogpoop", Virago.getInstance().getVersion());

        byte[] out = outString.getBytes(StandardCharsets.UTF_8);
        //int length = out.length;

        // # %%__USER_ID__%%

        //con.setFixedLengthStreamingMode(length);
        con.setRequestProperty("Authorization", this.authorization);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.connect();


        try (OutputStream os = con.getOutputStream()) {
            os.write(out);
        }

        if (!url.getHost().equals(con.getURL().getHost())) return "successful_authentication";

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();
        }
    }

    private String requestServerHTTPS(String productKey) throws IOException {
        URL url = new URL(server);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Safelock");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        String hwid = getMac();

        String outString = "{\"hwid\":\"password\",\"licensekey\":\"avain\",\"product\":\"NiceCar\",\"version\":\"dogpoop\"}";
        //Align HWID again here if someone tries to spoof it
        outString = outString
                .replaceAll("password", getHWID())
                .replaceAll("avain", productKey)
                .replaceAll("NiceCar", Virago.getInstance().getName())
                .replaceAll("dogpoop", Virago.getInstance().getVersion());

        byte[] out = outString.getBytes(StandardCharsets.UTF_8);

        con.setRequestProperty("Authorization", this.authorization);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.connect();

        try (OutputStream os = con.getOutputStream()) {
            os.write(out);
        }

        if (!url.getHost().equals(con.getURL().getHost())) return "successful_authentication";

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();
        }
    }

    public String[] isValid() {
        try {
            String response;
            if (server.contains("http")) {
                response = requestServer(productKey);
            } else {
                response = requestServerHTTPS(productKey);
            }

            if (!response.contains("{")) {
                GuiLicenceKey.status = "AUTHENTICATION FAILED";
                return new String[]{"1", "ODD_RESULT", "420"};
            }

            String hash = null;
            String version = null;
            String rank = null;
            String clientName = null;

            Gson gson = new Gson();
            var json = gson.fromJson(response, JsonObject.class);

            String neekeri = json.get("status_msg").getAsString();
            String status  = json.get("status_overview").getAsString();
            String statusCode = json.get("status_code").getAsString();

            if (status.contains("success")) {
                hash = json.get("status_id").toString().replaceAll("^\"|\"$", "").trim();
                version = json.get("version").toString();
                rank = json.get("tier").getAsString();
                clientName = json.get("clientname").getAsString();
            }

            if (hash != null && version != null) {
                String[] aa = hash.split("694201337");

                String hashed = aa[0];
                String decoded = new String(Base64.getDecoder().decode(hashed));

                if (!decoded.equals(productKey.substring(0, 2) + productKey.substring(productKey.length() - 2) + authorization.substring(0, 2))) {
                    GuiLicenceKey.status = "AUTHENTICATION FAILED";
                    return new String[]{"1", "FAILED_AUTHENTICATION", statusCode, String.valueOf(false)};
                }

                String time = String.valueOf(Instant.now().getEpochSecond());
                String unix = time.substring(0, time.length() - 2);

                long t = Long.parseLong(unix);
                long hashT = Long.parseLong(aa[1]);

                if (Math.abs(t - hashT) > 1) {
                    GuiLicenceKey.status = "AUTHENTICATION FAILED";
                    return new String[]{"1", "FAILED_AUTHENTICATION", statusCode, String.valueOf(false)};
                }
            }

            int statusLength = status.length();

            if (version != null && !version.equals("\"" + Virago.getInstance().getVersion() + "\"")
                    && status.contains("success") && response.contains("success")
                    && String.valueOf(statusLength).equals("7")) {
                GuiLicenceKey.status = "OUTDATED | LATEST VER: " + version.replaceAll("^\"|\"$", "");
                return new String[]{"3", "OUTDATED_VERSION#" + version, statusCode, String.valueOf(true)};
            }

            if (neekeri.contains("BLACKLISTED")) {
                GuiLicenceKey.status = "YOU ARE BLACKLISTED";
                return new String[]{"1", neekeri, statusCode, String.valueOf(false)};
            }

            statusLength = status.length();

            if (!isValidLength(statusLength)) {
                GuiLicenceKey.status = "AUTHENTICATION FAILED";
                return new String[]{"1", neekeri, statusCode, String.valueOf(false)};
            }

            //Minecraft.getMinecraft().displayGuiScreen(new GuiLicenceKey());
            final boolean valid = status.contains("success") && response.contains("success") && String.valueOf(statusLength).equals("7");
            GuiLicenceKey.isAuthorized = valid;
            Virago.getInstance().setViragoUser(new ViragoUser(clientName, "0001", Rank.getRank(rank)));
            if (valid) {
                Minecraft.getMinecraft().displayGuiScreen(new CustomGuiMainMenu());
            }
            return new String[]{valid ? "2" : "1", neekeri, statusCode, String.valueOf(valid)};
        } catch (IOException ex) {
            if (ex.getMessage().contains("429")) {
                GuiLicenceKey.status = "RATE_LIMITED";
                return new String[]{"1", "ERROR", "You are being rate limited because of sending too many requests", String.valueOf(false)};
            }
            ex.printStackTrace();
            GuiLicenceKey.status = "ERROR";
            return new String[]{"1", "ERROR", ex.getMessage(), String.valueOf(false)};
        }
    }

    public boolean isValidLength(int reps) {
        return reps == 7;
    }

    //Spoofed methods to trick cracker

    // # %%__USER_ID__%%

    public boolean isValidLength22(int reps) {
        return reps == 11;
    }

    public boolean isValidLength222(int reps) {
        return reps == 44;
    }

    public boolean isValidLength2222(int reps) {
        return reps == 48;
    }

    public String getMac() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        StringBuilder sb = new StringBuilder();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            byte[] hardwareAddress = ni.getHardwareAddress();
            if (hardwareAddress != null) {
                for (byte address : hardwareAddress) {
                    sb.append(String.format("%02X", address));
                }
                return sb.toString();
            }
        }

        return null;
    }

    public static String getHWID() {
        try {
            if (isWindows()) {
                return getWindowsIdentifier();
            } else if (isMac()) {
                return getMacOsIdentifier();
            } else if (isLinux()) {
                return getLinuxMacAddress();
            } else {
                return UNKNOWN;
            }
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    private static boolean isWindows() {
        return (OS.contains("win"));
    }

    private static boolean isMac() {
        return (OS.contains("mac"));
    }

    private static boolean isLinux() {
        return (OS.contains("inux"));
    }

    private static String getLinuxMacAddress() throws FileNotFoundException, NoSuchAlgorithmException {
        File machineId = new File("/var/lib/dbus/machine-id");
        if (!machineId.exists()) {
            machineId = new File("/etc/machine-id");
        }
        if (!machineId.exists()) {
            return UNKNOWN;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(machineId);
            String id = scanner.useDelimiter("\\A").next();
            return hexStringify(sha256Hash(id.getBytes()));
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static String getMacOsIdentifier() throws SocketException, NoSuchAlgorithmException {
        NetworkInterface networkInterface = NetworkInterface.getByName("en0");
        byte[] hardwareAddress = networkInterface.getHardwareAddress();
        return hexStringify(sha256Hash(hardwareAddress));
    }

    private static String getWindowsIdentifier() throws IOException, NoSuchAlgorithmException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(new String[]{"wmic", "csproduct", "get", "UUID"});

        String result = null;
        InputStream is = process.getInputStream();
        Scanner sc = new Scanner(process.getInputStream());
        try {
            while (sc.hasNext()) {
                String next = sc.next();
                if (next.contains("UUID")) {
                    result = sc.next().trim();
                    break;
                }
            }
        } finally {
            is.close();
        }

        return result == null ? UNKNOWN : hexStringify(sha256Hash(result.getBytes()));
    }

    /**
     * Compute the SHA-256 hash of the given byte array
     *
     * @param data the byte array to hash
     * @return the hashed byte array
     * @throws NoSuchAlgorithmException
     */
    public static byte[] sha256Hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return messageDigest.digest(data);
    }

    /**
     * Convert a byte array to its hex-string
     *
     * @param data the byte array to convert
     * @return the hex-string of the byte array
     */
    public static String hexStringify(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte singleByte : data) {
            stringBuilder.append(Integer.toString((singleByte & 0xff) + 0x100, 16).substring(1));
        }

        return stringBuilder.toString();
    }

}