package fr.leomelki.loupgarou.cli;

import org.bukkit.command.CommandSender;

class ParserConfig extends ParserAbstract {
    protected ParserConfig(CommandInterpreter other) {
        super(other);
    }

    /* ========================================================================== */
    /*                                RELOAD CONFIG                               */
    /* ========================================================================== */

    protected void processReloadConfig(CommandSender sender) {
        sender.sendMessage("\n§aVous avez bien reload la config !");
        sender.sendMessage("§7§oSi vous avez changé les rôles, écriver §8§o/lg joinall§7§o !");
        this.instanceMainLg.loadConfig();
    }
}
