package org.ssoggy.ssoggysouls.util;

import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

@Mod.EventBusSubscriber(modid = SSoggySoulsMod.MODID)
public class SchedulerManager extends AbstractSchedulerManager {
    
    private SchedulerManager() {
        // Utility class
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        tickTasks(e -> SSoggySoulsMod.LOGGER.error("Error executing scheduled task", e));
    }
}
