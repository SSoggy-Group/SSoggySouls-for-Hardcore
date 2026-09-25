package org.ssoggy.ssoggysouls.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

@Mod.EventBusSubscriber(modid = SSoggySoulsMod.MODID)
public class SchedulerManager extends AbstractSchedulerManager {
    
    private SchedulerManager() {
        // Utility class
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        tickTasks(e -> SSoggySoulsMod.LOGGER.error("Error executing scheduled task", e));
    }
}
