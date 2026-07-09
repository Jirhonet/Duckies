package net.jirho.duckies.fabric;

import net.fabricmc.api.ModInitializer;
import net.jirho.duckies.Duckies;

public class DuckiesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Duckies.init();
    }
}
