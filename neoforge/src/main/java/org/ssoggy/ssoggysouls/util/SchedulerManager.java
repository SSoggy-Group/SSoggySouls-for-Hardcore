package org.ssoggy.ssoggysouls.util;

import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

public class SchedulerManager extends AbstractSchedulerManager {
    
    private SchedulerManager() {
        // Utility class
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onServerTick(ServerTickEvent.Post event) {
        tickTasks(e -> SSoggySoulsMod.LOGGER.error("Error executing scheduled task", e));
    }
}
