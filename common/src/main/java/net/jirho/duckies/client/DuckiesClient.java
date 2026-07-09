package net.jirho.duckies.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jirho.duckies.client.renderer.DuckRenderer;
import net.jirho.duckies.client.renderer.model.DuckModel;
import net.jirho.duckies.init.DuckiesRegistries;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public final class DuckiesClient {
    private DuckiesClient() {
    }

    public static void init() {
        EntityModelLayerRegistry.register(DuckModel.LAYER_LOCATION, DuckModel::createBodyLayer);
        EntityRendererRegistry.register(DuckiesRegistries.DUCK, DuckRenderer::new);
    }
}
