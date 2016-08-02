package com.settingdust.deathban;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: SettingDust
 * Date: 16-7-31
 * By IntelliJ IDEA
 */
public class DeathBan extends JavaPlugin implements Listener {
    int time = 0;
    Map<String, Object> list = new HashMap<String, Object>();

    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.time = getConfig().getInt("time");
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        this.saveConfig();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!event.getEntity().hasPermission("deathban.unban")) {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            calendar.setTime(date);
            calendar.add(Calendar.SECOND, time);
            date = calendar.getTime();
            list.put(event.getEntity().getName(), format.format(date));
            event.getEntity().kickPlayer(getConfig().getString("death-message")
                    .replace("{time}", (String) list.get(event.getEntity().getName()))
                    .replace("&", "§"));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("deathban.unban")
                && list.containsKey(event.getPlayer().getName())) {
            try {
                Date now = new Date();
                SimpleDateFormat format = new SimpleDateFormat("");
                Date end = format.parse((String) list.get(event.getPlayer().getName()));
                long between = (end.getTime() - now.getTime()) / 1000;
                long day = between / (24 * 3600);
                long hour = between % (24 * 3600) / 3600;
                long minute = between % 3600 / 60;
                long second = between % 60 / 60;
                if (between > 0L) {
                    String s = "";
                    if (day != 0) s = s + day + "天";
                    if (hour != 0) s = s + hour + "时";
                    if (minute != 0) s = s + minute + "分";
                    if (second != 0) s = s + second + "秒";
                    event.getPlayer().kickPlayer(getConfig().getString("kick-message")
                            .replace("{time}", s).replace("&", "§"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("deathban")
                && sender.hasPermission("deathban.command")) {
            if (args.length == 2 && args[0].equalsIgnoreCase("unban")) {
                list.remove(args[1]);
                sender.sendMessage(ChatColor.AQUA + "解ban成功");
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                this.reloadConfig();
            }
        }
        return false;
    }
}
