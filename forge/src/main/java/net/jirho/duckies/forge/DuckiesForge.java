package net.jirho.duckies.forge;

import dev.architectury.platform.forge.EventBuses;
import net.jirho.duckies.Duckies;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Duckies.MOD_ID)
public class DuckiesForge {
    public DuckiesForge() {
        EventBuses.registerModEventBus(Duckies.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Duckies.init();
    }
}
