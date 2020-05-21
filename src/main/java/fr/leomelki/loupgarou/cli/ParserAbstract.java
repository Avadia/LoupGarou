package fr.leomelki.loupgarou.cli;

import fr.leomelki.loupgarou.MainLg;
import org.bukkit.command.CommandSender;

abstract class ParserAbstract {
    protected static final String PLAYER_ERROR_PREFIX = "\n§4Erreur : §cLe joueur §4";
    final MainLg instanceMainLg;

    protected ParserAbstract(MainLg instanceMainLg) {
        this.instanceMainLg = instanceMainLg;
    }

    protected ParserAbstract(CommandInterpreter other) {
        this.instanceMainLg = other.instanceMainLg;
    }

    protected boolean isAuthorized(CommandSender sender) {
        return sender.hasPermission("loupgarou.admin");
    }

    protected void denyCommand(CommandSender sender) {
        sender.sendMessage("§4Erreur: Vous n'avez pas la permission...");
    }

    protected void helpCommand(CommandSender sender) {
        sender.sendMessage("§4Erreur: §cCommande incorrecte.");
        sender.sendMessage("§4Essayez /lg §caddSpawn/end/start/nextNight/nextDay/reloadConfig/roles/joinAll/nick/unnick");
    }

    protected Integer parseInteger(String raw) {
        try {
            final int parsedValue = Integer.parseInt(raw);

            return (parsedValue >= 0) ? parsedValue : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
