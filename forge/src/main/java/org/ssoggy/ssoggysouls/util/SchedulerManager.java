package org.ssoggy.ssoggysouls.util;

import net.minecraftforge.event.TickEvent;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

public class SchedulerManager extends AbstractSchedulerManager {
    
    private SchedulerManager() {
        // Utility class
    }

    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        tickTasks(e -> SSoggySoulsMod.LOGGER.error("Error executing scheduled task", e));
    }
}
