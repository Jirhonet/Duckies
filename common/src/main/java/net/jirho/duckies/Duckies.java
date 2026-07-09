package net.jirho.duckies;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.jirho.duckies.client.DuckiesClient;
import net.jirho.duckies.common.entity.Duck;
import net.jirho.duckies.init.DuckiesRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public final class Duckies {
    public static final String MOD_ID = "duckies";

    private Duckies() {
    }

    public static void init() {
        DuckiesRegistries.register();

        LifecycleEvent.SETUP.register(() -> {
            SpawnPlacements.register(
                    DuckiesRegistries.DUCK.get(),
                    Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Duck::canSpawn
            );
        });

        BiomeModifications.addProperties(
                (ctx, mutable) -> {
                    if (ctx.getKey().map(key -> key.equals(new ResourceLocation("minecraft:river"))).orElse(false)) {
                        mutable.getSpawnProperties().addSpawn(
                                net.minecraft.world.entity.MobCategory.CREATURE,
                                new MobSpawnSettings.SpawnerData(DuckiesRegistries.DUCK.get(), 20, 2, 7)
                        );
                    }
                }
        );

        EnvExecutor.runInEnv(Env.CLIENT, () -> DuckiesClient::init);
    }
}
