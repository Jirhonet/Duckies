package net.jirho.duckies.client;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jirho.duckies.client.renderer.DuckRenderer;
import net.jirho.duckies.client.renderer.model.DuckModel;
import net.jirho.duckies.init.DuckiesRegistries;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.FoliageColor;

@Environment(EnvType.CLIENT)
public final class DuckiesClient {
    private DuckiesClient() {
    }

    public static void init() {
        EntityModelLayerRegistry.register(DuckModel.LAYER_LOCATION, DuckModel::createBodyLayer);
        EntityModelLayerRegistry.register(DuckModel.BABY_LAYER_LOCATION, DuckModel::createBabyBodyLayer);
        EntityRendererRegistry.register(DuckiesRegistries.DUCK, DuckRenderer::new);
        ClientLifecycleEvent.CLIENT_SETUP.register(
                minecraft -> RenderTypeRegistry.register(RenderType.cutout(), DuckiesRegistries.DUCKWEED.get()));
        ColorHandlerRegistry.registerBlockColors((state, level, pos, tintIndex) -> {
            if (level != null && pos != null) {
                return BiomeColors.getAverageFoliageColor(level, pos);
            }
            return FoliageColor.FOLIAGE_DEFAULT;
        }, DuckiesRegistries.DUCKWEED);
        // Item tinting in 1.21.4+ is data-driven (item model tints), so we don't
        // register legacy item color handlers here.
    }
}
