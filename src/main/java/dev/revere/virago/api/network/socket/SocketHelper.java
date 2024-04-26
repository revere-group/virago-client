package dev.revere.virago.api.network.socket;

import com.google.gson.Gson;
import lombok.Setter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class SocketHelper {
    public static void createSocketConnection(URI uri, WebSocketHandler handler) {
        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                handler.onOpen(serverHandshake);
            }

            @Override
            public void onMessage(String s) {
                handler.onMessage(s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                handler.onClose(i, s, b);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        };

        handler.setClient(client);
        client.connect();
    }

    @Setter
    public static class WebSocketHandler {
        private WebSocketClient client;
        private static final Gson gson = new Gson();

        public void onOpen(ServerHandshake serverHandshake) {}
        public void onMessage(String s) {}
        public void onClose(int i, String s, boolean b) {}
        public void onError(Exception e) {}

        /**
         * Send an object to the client.
         * It will be serialized into JSON.
         * @param object The object to send.
         */
        protected void send(Object object) {
            client.send(gson.toJson(object));
        }

        /**
         * Deserialize the packet from JSON.
         * @param packet The packet as a string
         * @param clazz The packet type
         * @return The deserialized packet
         * @param <T> The type of the packet
         */
        protected <T> T deserialize(String packet, Class<T> clazz) {
            return gson.fromJson(packet, clazz);
        }

        /**
         * Log to the console.
         */
        protected void log(String value) {
            System.out.printf("[SOCKET] [LOG]: %s\n", value);
        }

        /**
         * Close the current session.
         */
        protected void close() {
            client.close();
        }
    }
}
