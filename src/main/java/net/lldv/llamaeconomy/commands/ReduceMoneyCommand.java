package net.lldv.llamaeconomy.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.ConfigSection;
import net.lldv.llamaeconomy.LlamaEconomy;
import net.lldv.llamaeconomy.components.event.PlayerReduceMoneyEvent;
import net.lldv.llamaeconomy.components.language.Language;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReduceMoneyCommand extends PluginCommand<LlamaEconomy> {

    public ReduceMoneyCommand(LlamaEconomy owner, ConfigSection section) {
        super(section.getString("name"), owner);
        setDescription(section.getString("description"));
        setUsage(section.getString("usage"));
        setAliases(section.getStringList("aliases").toArray(new String[]{}));
        setPermission(section.getString("permission"));
        addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.STRING, false),
                new CommandParameter("amount", CommandParamType.FLOAT, false)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (!sender.hasPermission(getPermission())) return false;
        CompletableFuture.runAsync(() -> {
            if (args.length >= 2) {
                try {
                    String target = args[0];
                    Player playerTarget = getPlugin().getServer().getPlayer(target);
                    if (playerTarget != null) target = playerTarget.getName();

                    if (!LlamaEconomy.getAPI().hasAccount(target)) {
                        sender.sendMessage(Language.get("not-registered", target));
                        return;
                    }

                    double amt = Double.parseDouble(args[1]);

                    if (amt < 0) {
                        sender.sendMessage(Language.get("invalid-amount"));
                        return;
                    }

                    LlamaEconomy.getAPI().reduceMoney(target, amt);
                    Server.getInstance().getPluginManager().callEvent(new PlayerReduceMoneyEvent(target, sender.getName(), amt));
                    sender.sendMessage(Language.get("reduced-money", target, getPlugin().getMonetaryUnit(), amt));

                } catch (NumberFormatException ex) {
                    sender.sendMessage(Language.get("invalid-amount"));
                }
            } else sender.sendMessage(Language.get("usage", getUsage()));
        });
        return false;
    }

}
