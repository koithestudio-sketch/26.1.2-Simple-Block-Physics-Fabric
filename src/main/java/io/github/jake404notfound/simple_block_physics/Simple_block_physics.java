package io.github.jake404notfound.simple_block_physics;

import io.github.jake404notfound.simple_block_physics.utils.TickHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class Simple_block_physics implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigManager.init();

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            TickHandler.onServerTick();
        });

        ServerTickEvents.START_WORLD_TICK.register(world -> {
            TickHandler.onLevelTick(world);
        });
    }
}
