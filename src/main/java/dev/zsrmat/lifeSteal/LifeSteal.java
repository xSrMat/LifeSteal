package dev.zsrmat.lifeSteal;

import dev.zsrmat.lifeSteal.commands.LifeStealCommand;
import dev.zsrmat.lifeSteal.LifeStealExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public final class LifeSteal extends JavaPlugin implements Listener {

    private HashMap<UUID, Integer> playerLives = new HashMap<>();
    private FileConfiguration config;
    private int maxLives;
    private int initialLives;

    @Override
    public void onEnable() {
        // Cargar configuración
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", true);
        }
        config = getConfig();
        maxLives = config.getInt("max-lives", 20);
        initialLives = config.getInt("initial-lives", 3);

        // Cargar datos existentes
        config.getKeys(false).forEach(key -> {
            try {
                UUID uuid = UUID.fromString(key);
                playerLives.put(uuid, config.getInt(key));
            } catch (IllegalArgumentException ignored) {}
        });

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("lifesteal").setExecutor(new LifeStealCommand(this));

        // Registrar PlaceholderAPI si está presente
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LifeStealExpansion(this).register();
            getLogger().info("PlaceholderAPI integrated correctly!");
        }

        getLogger().info("LifeSteal activado!");
    }

    @Override
    public void onDisable() {
        saveData();
        getLogger().info("Datos guardados correctamente!");
    }

    private void saveData() {
        playerLives.forEach((uuid, lives) -> config.set(uuid.toString(), lives));
        saveConfig();
    }

    public void addLives(OfflinePlayer player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentLives = playerLives.getOrDefault(uuid, initialLives);
        int newLives = Math.min(currentLives + amount, maxLives);

        playerLives.put(uuid, newLives);
        savePlayerData(uuid);
    }

    public void removeLives(OfflinePlayer player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentLives = playerLives.getOrDefault(uuid, initialLives);
        int newLives = Math.max(currentLives - amount, 0);

        playerLives.put(uuid, newLives);
        savePlayerData(uuid);
    }

    private void savePlayerData(UUID uuid) {
        config.set(uuid.toString(), playerLives.get(uuid));
        saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!playerLives.containsKey(uuid)) {
            playerLives.put(uuid, initialLives);
            savePlayerData(uuid);

            String message = config.getString("messages.welcome", "§6§lLifeSteal §r§eBienvenido! Comienzas con §a%lives% vidas§e.");
            if (message != null) {
                message = message.replace("%lives%", String.valueOf(initialLives));
                player.sendMessage(message);
            } else {
                player.sendMessage("§6§lLifeSteal §r§eBienvenido! Comienzas con §a" + initialLives + "§e vidas§e.");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !victim.equals(killer)) {
            handleVictim(victim);
            handleKiller(killer);
        } else {
            String mobDeathMessage = config.getString("messages.mob-death", "§cHas sido matado por un mob!");
            if (mobDeathMessage != null) {
                victim.sendMessage(mobDeathMessage);
            } else {
                victim.sendMessage("§cHas sido matado por un mob!");
            }
            handleVictim(victim);
        }
    }

    private void handleVictim(Player victim) {
        UUID uuid = victim.getUniqueId();
        int currentLives = playerLives.getOrDefault(uuid, initialLives);
        int newLives = currentLives - 1;

        playerLives.put(uuid, newLives);
        savePlayerData(uuid);

        String lossMessage = config.getString("messages.life-lost", "§cHas perdido una vida! §eVidas restantes: §a%lives%");
        if (lossMessage != null) {
            lossMessage = lossMessage.replace("%lives%", String.valueOf(newLives));
            victim.sendMessage(lossMessage);
        } else {
            victim.sendMessage("§cHas perdido una vida! §eVidas restantes: §a" + newLives);
        }

        if (newLives <= 0) {
            banPlayer(victim);
        } else {
            String warningMessage = config.getString("messages.life-warning", "§e¡Cuidado! Te quedan §a%lives% vidas§e.");
            if (warningMessage != null) {
                warningMessage = warningMessage.replace("%lives%", String.valueOf(newLives));
                victim.sendMessage(warningMessage);
            } else {
                victim.sendMessage("§e¡Cuidado! Te quedan §a" + newLives + "§e vidas§e.");
            }
        }
    }

    private void handleKiller(Player killer) {
        UUID uuid = killer.getUniqueId();
        int currentLives = playerLives.getOrDefault(uuid, initialLives);
        int newLives = Math.min(currentLives + 1, maxLives);

        playerLives.put(uuid, newLives);
        savePlayerData(uuid);

        String gainMessage = config.getString("messages.life-gained", "§a+1 vida! §eAhora tienes: §2%lives% vidas§e.");
        if (gainMessage != null) {
            gainMessage = gainMessage.replace("%lives%", String.valueOf(newLives));
            killer.sendMessage(gainMessage);
        } else {
            killer.sendMessage("§a+1 vida! §eAhora tienes: §2" + newLives + "§e vidas§e.");
        }

        String title = config.getString("messages.life-gained-title", "§l§aVIDA +1");
        String subtitle = config.getString("messages.life-gained-subtitle", "§7Total: %lives%");
        if (subtitle != null) {
            subtitle = subtitle.replace("%lives%", String.valueOf(newLives));
        } else {
            subtitle = "§7Total: " + newLives;
        }
        killer.sendTitle(title, subtitle, 10, 70, 20);

        if (newLives >= maxLives) {
            String maxMessage = config.getString("messages.max-lives", "§a¡Has alcanzado el máximo de vidas!");
            if (maxMessage != null) {
                killer.sendMessage(maxMessage);
            } else {
                killer.sendMessage("§a¡Has alcanzado el máximo de vidas!");
            }
        }
    }

    private void banPlayer(Player player) {
        String banMessage = config.getString("messages.ban-message", "¡Has perdido todas tus vidas!");
        if (banMessage != null) {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                    player.getName(),
                    banMessage,
                    null,
                    "LifeSteal"
            );
            player.kickPlayer(banMessage);
        } else {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                    player.getName(),
                    "¡Has perdido todas tus vidas!",
                    null,
                    "LifeSteal"
            );
            player.kickPlayer("¡Has perdido todas tus vidas!");
        }
    }

    public int getLives(OfflinePlayer player) {
        return playerLives.getOrDefault(player.getUniqueId(), initialLives);
    }
}