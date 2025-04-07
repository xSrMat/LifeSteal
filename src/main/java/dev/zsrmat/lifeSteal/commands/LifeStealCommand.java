package dev.zsrmat.lifeSteal.commands;

import dev.zsrmat.lifeSteal.LifeSteal;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LifeStealCommand implements CommandExecutor {

    private final LifeSteal plugin;

    public LifeStealCommand(LifeSteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int lives = plugin.getLives(player);
                String selfMessage = plugin.getConfig().getString("messages.check-self", "§eTus vidas: §a%lives%")
                        .replace("%lives%", String.valueOf(lives));
                sender.sendMessage(selfMessage);
                return true;
            }
            sender.sendMessage("§cUso: /" + label + " <jugador>");
            return true;
        }

        if (args.length == 1) {
            if (sender.hasPermission("lifesteal.checkothers")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                if (target.hasPlayedBefore()) {
                    int lives = plugin.getLives(target);
                    String otherMessage = plugin.getConfig().getString("messages.check-other", "§eVidas de %player%: §a%lives%")
                            .replace("%player%", target.getName())
                            .replace("%lives%", String.valueOf(lives));
                    sender.sendMessage(otherMessage);
                } else {
                    sender.sendMessage(plugin.getConfig().getString("messages.player-not-found", "§cJugador no encontrado!"));
                }
                return true;
            }

            sender.sendMessage(plugin.getConfig().getString("messages.no-permission", "§cNo tienes permiso para esto!"));
            return true;
        }

        if (args.length >= 3 && sender.hasPermission("lifesteal.admin")) {
            if (args[0].equalsIgnoreCase("add")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target.hasPlayedBefore()) {
                    try {
                        int amount = Integer.parseInt(args[2]);
                        plugin.addLives(target, amount);
                        sender.sendMessage(plugin.getConfig().getString("messages.lives-added", "§aSe han agregado §e%d§a vidas a %player%.")
                                .replace("%d", String.valueOf(amount))
                                .replace("%player%", target.getName()));
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cLa cantidad debe ser un número.");
                    }
                } else {
                    sender.sendMessage(plugin.getConfig().getString("messages.player-not-found", "§cJugador no encontrado!"));
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target.hasPlayedBefore()) {
                    try {
                        int amount = Integer.parseInt(args[2]);
                        plugin.removeLives(target, amount);
                        sender.sendMessage(plugin.getConfig().getString("messages.lives-removed", "§cSe han quitado §e%d§c vidas de %player%.")
                                .replace("%d", String.valueOf(amount))
                                .replace("%player%", target.getName()));
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cLa cantidad debe ser un número.");
                    }
                } else {
                    sender.sendMessage(plugin.getConfig().getString("messages.player-not-found", "§cJugador no encontrado!"));
                }
            } else {
                sender.sendMessage("§cUso: /lifesteal add/remove <jugador> <cantidad>");
            }
            return true;
        }

        sender.sendMessage("§cUso: /lifesteal <jugador> o /lifesteal add/remove <jugador> <cantidad>");
        return true;
    }
}
