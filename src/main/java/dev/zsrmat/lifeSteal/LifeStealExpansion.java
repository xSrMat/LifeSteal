package dev.zsrmat.lifeSteal;

import dev.zsrmat.lifeSteal.LifeSteal;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LifeStealExpansion extends PlaceholderExpansion {

    private final LifeSteal plugin;

    public LifeStealExpansion(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lifesteal";
    }

    @Override
    public @NotNull String getAuthor() {
        return "zsrmat";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("lives")) {
            return String.valueOf(plugin.getLives(player));
        }
        return null;
    }
}