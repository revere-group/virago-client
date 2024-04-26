package dev.revere.virago;

import dev.revere.virago.api.event.core.EventBus;
import dev.revere.virago.api.network.packet.client.C2SUpdate;
import dev.revere.virago.api.protection.ViragoUser;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.api.service.ServiceManager;
import dev.revere.virago.api.network.socket.SocketClient;
import dev.revere.virago.api.network.socket.SocketHelper;
import dev.revere.virago.client.gui.panel.PanelGUI;
import dev.revere.virago.client.services.*;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.misc.DiscordRPC;
import dev.revere.virago.util.input.KeybindManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.net.URI;

import static dev.revere.virago.api.network.socket.SocketClient.getURL;
import static dev.revere.virago.api.network.socket.SocketClient.key;
import static dev.revere.virago.api.network.socket.SocketHelper.createSocketConnection;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */

@Getter
@Setter
public class Virago {
    @Getter private static final Virago instance = new Virago();

    private final String name = "Virago";
    private final String version = "1.0.0";
    private final String author = "Revere";

    private final File clientDir = new File(Minecraft.getMinecraft().mcDataDir, getName().toLowerCase());

    private ServiceManager serviceManager;
    private ViragoUser viragoUser;
    private DiscordRPC discordRPC;
    private PanelGUI panelGUI;
    private EventBus eventBus;

    /**
     * Starts all services and registers all events
     */
    public void startVirago() {
        Logger.info("Starting Virago...", getClass());
        this.discordRPC = new DiscordRPC();

        if (!clientDir.exists() && clientDir.mkdir())
            Logger.info("Created client directory.", getClass());

        handleEvents();
        handleServices();
        handleManagers();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(SocketClient.jwt != null) {
                createSocketConnection(URI.create(getURL("/auth/logout")), new SocketHelper.WebSocketHandler() {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        send(new C2SUpdate(key));
                    }
                });
            }
        }));
    }

    /**
     * Stops all services
     */
    public void stopVirago() {
        Logger.info("Stopping Virago...", getClass());
        this.serviceManager.getServices().values().forEach(IService::stopService);
        this.serviceManager.getServices().values().forEach(IService::destroyService);
    }

    /**
     * Initializes all managers
     */
    private void handleManagers() {
        this.panelGUI = new PanelGUI();
    }

    /**
     * Initializes and registers all events
     */
    private void handleEvents() {
        this.eventBus = new EventBus();
        this.eventBus.register(new KeybindManager());
    }

    /**
     * Initializes and starts all services
     */
    private void handleServices() {
        this.serviceManager = new ServiceManager();
        this.serviceManager.addService(new NotificationService());
        this.serviceManager.addService(new CommandService());
        this.serviceManager.addService(new ModuleService());
        this.serviceManager.addService(new DraggableService());
        this.serviceManager.addService(new FontService());
        this.serviceManager.addService(new ConfigService());
        this.serviceManager.addService(new AltService());
        this.serviceManager.addService(new FriendService());
        this.serviceManager.addService(new CheckService());
        this.serviceManager.getServices().values().forEach(IService::initService);
        this.serviceManager.getServices().values().forEach(IService::startService);
    }
}
