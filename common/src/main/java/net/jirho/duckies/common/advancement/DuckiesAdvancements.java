package net.jirho.duckies.common.advancement;

import net.jirho.duckies.Duckies;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class DuckiesAdvancements {
    public static final ResourceLocation WHAT_THE_DUCK = ResourceLocation.fromNamespaceAndPath(Duckies.MOD_ID, "what_the_duck");
    public static final ResourceLocation LEMONADE_STAND = ResourceLocation.fromNamespaceAndPath(Duckies.MOD_ID, "lemonade_stand");
    public static final ResourceLocation PEACE_WAS_NEVER_AN_OPTION = ResourceLocation.fromNamespaceAndPath(Duckies.MOD_ID,
            "peace_was_never_an_option");
    public static final ResourceLocation I_LOVE_YOU = ResourceLocation.fromNamespaceAndPath(Duckies.MOD_ID, "i_love_you");

    private DuckiesAdvancements() {
    }

    public static void grant(ServerPlayer player, ResourceLocation advancementId, String criterion) {
        AdvancementHolder advancement = player.server.getAdvancements().get(advancementId);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
