package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.update.PreMotionEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

@ModuleData(name = "Ambience", description = "Change ambience", type = EnumModuleType.RENDER)
public class Ambience extends AbstractModule {

    private final Setting<Integer> time = new Setting<>("Time", 1000)
            .minimum(0)
            .maximum(22999)
            .incrementation(100);

    private final Setting<WeatherMode> weather = new Setting<>("Weather", WeatherMode.UNCHANGED);

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        if (event.getEventState() == PacketEvent.EventState.RECEIVING) {
            Packet<INetHandlerPlayClient> packet = event.getPacket();
            if (packet instanceof S03PacketTimeUpdate) {
                event.setCancelled(true);
            }
        }
    };

    @EventHandler
    private final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.theWorld.setWorldTime(time.getValue());

        switch(this.weather.getValue()) {
            case CLEAR: {
                mc.theWorld.setRainStrength(0);
                mc.theWorld.getWorldInfo().setCleanWeatherTime(Integer.MAX_VALUE);
                mc.theWorld.getWorldInfo().setRainTime(0);
                mc.theWorld.getWorldInfo().setThunderTime(0);
                mc.theWorld.getWorldInfo().setRaining(false);
                mc.theWorld.getWorldInfo().setThundering(false);
                break;
            }

            case RAIN: {
                mc.theWorld.setRainStrength(1);
                mc.theWorld.getWorldInfo().setCleanWeatherTime(0);
                mc.theWorld.getWorldInfo().setRainTime(Integer.MAX_VALUE);
                mc.theWorld.getWorldInfo().setThunderTime(0);
                mc.theWorld.getWorldInfo().setRaining(true);
                mc.theWorld.getWorldInfo().setThundering(false);
                break;
            }

            case STORM: {
                mc.theWorld.setRainStrength(1);
                mc.theWorld.setThunderStrength(1);
                mc.theWorld.getWorldInfo().setCleanWeatherTime(0);
                mc.theWorld.getWorldInfo().setRainTime(Integer.MAX_VALUE);
                mc.theWorld.getWorldInfo().setThunderTime(Integer.MAX_VALUE);
                mc.theWorld.getWorldInfo().setRaining(true);
                mc.theWorld.getWorldInfo().setThundering(true);
                break;
            }

            case SNOW: {
                mc.theWorld.setRainStrength(0);
                mc.theWorld.getWorldInfo().setCleanWeatherTime(0);
                mc.theWorld.getWorldInfo().setRainTime(Integer.MAX_VALUE);
                mc.theWorld.getWorldInfo().setThunderTime(0);
                mc.theWorld.getWorldInfo().setRaining(true);
                mc.theWorld.getWorldInfo().setThundering(false);
                break;
            }
        }
    };




    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.theWorld.setRainStrength(0);
        mc.theWorld.getWorldInfo().setCleanWeatherTime(Integer.MAX_VALUE);
        mc.theWorld.getWorldInfo().setRainTime(0);
        mc.theWorld.getWorldInfo().setThunderTime(0);
        mc.theWorld.getWorldInfo().setRaining(false);
        mc.theWorld.getWorldInfo().setThundering(false);
        super.onDisable();
    }

    private enum WeatherMode {
        UNCHANGED, CLEAR, RAIN, STORM, SNOW
    }

}
