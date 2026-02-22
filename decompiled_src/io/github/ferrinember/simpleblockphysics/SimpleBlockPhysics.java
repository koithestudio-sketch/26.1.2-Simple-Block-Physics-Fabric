/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.fml.config.IConfigSpec
 *  net.neoforged.fml.config.ModConfig$Type
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.server.ServerStartingEvent
 *  org.slf4j.Logger
 */
package io.github.ferrinember.simpleblockphysics;

import com.mojang.logging.LogUtils;
import io.github.ferrinember.simpleblockphysics.Config;
import io.github.ferrinember.simpleblockphysics.utils.BlockPlaceHandler;
import io.github.ferrinember.simpleblockphysics.utils.TickHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(value="simpleblockphysics")
public class SimpleBlockPhysics {
    public static final String MOD_ID = "simpleblockphysics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpleBlockPhysics(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register((Object)this);
        NeoForge.EVENT_BUS.register((Object)new BlockPlaceHandler());
        NeoForge.EVENT_BUS.register((Object)new TickHandler());
        modContainer.registerConfig(ModConfig.Type.COMMON, (IConfigSpec)Config.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
