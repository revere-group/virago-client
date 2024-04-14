package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.FriendService;
import dev.revere.virago.client.services.NotificationService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/7/2024
 */
@ModuleData(name = "MCF", description = "Middle click to add friends", type = EnumModuleType.PLAYER)
public class MiddleClickFriend extends AbstractModule {

    private boolean wasDown;

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        if(!mc.inGameHasFocus) return;

        boolean down = mc.gameSettings.keyBindPickBlock.isKeyDown();
        NotificationService notificationService = Virago.getInstance().getServiceManager().getService(NotificationService.class);
        FriendService friendService = Virago.getInstance().getServiceManager().getService(FriendService.class);

        if(down && !wasDown) {
            if(mc.objectMouseOver == null || !(mc.objectMouseOver.entityHit instanceof EntityPlayer)) return;

            EntityPlayer player = (EntityPlayer) mc.objectMouseOver.entityHit;
            String name = StringUtils.stripControlCodes(player.getName());

            if(friendService.isFriend(name)) {
                friendService.getFriends().removeIf(friend -> friend.equalsIgnoreCase(name));
                notificationService.notify(NotificationType.INFO, "Friend Manager", "You have removed " + name + " from your friend list.");
                return;
            }

            friendService.getFriends().add(name);
            notificationService.notify(NotificationType.INFO, "Friend Manager", "You have added " + name + " to your friend list.");
            wasDown = true;
        } else if(!down) {
            wasDown = false;
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
