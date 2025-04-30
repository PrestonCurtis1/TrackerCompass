package me.Unprankable.trackerCompass;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;  // For Paper's NBT handling
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class TrackerCompass extends JavaPlugin implements CommandExecutor, Listener {
    boolean autoUpdateCompass = true;
    @Override
    public void onEnable() {
        this.getCommand("track").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        if (autoUpdateCompass){
            new BukkitRunnable() {
                @Override
                public void run() {
                    autoUpdate(); // Call your tick function
                }
            }.runTaskTimer(this, 0L, 100L);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /track <player>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + args[0] + " is not online.");
            return true;
        }
        giveCompass(target, player);
        player.sendMessage(ChatColor.GREEN + "Given you a tracker for " + ChatColor.YELLOW + target.getName());
        return true;
    }

    public void giveCompass(Player target, Player receiver) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Tracker: " + ChatColor.AQUA + target.getName());
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GREEN + "World: " + ChatColor.GREEN + target.getWorld().getName());
        lore.add(ChatColor.YELLOW + "Location: " + ChatColor.GOLD +  Math.round(target.getLocation().getX()) + ", " + Math.round(target.getLocation().getY()) + ", " + Math.round(target.getLocation().getZ()));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(new NamespacedKey(this, "tracking"), PersistentDataType.STRING, target.getName());
        Location lodestone = target.getLocation();
        meta.setLodestone(lodestone);
        meta.setLodestoneTracked(false);
        compass.setItemMeta(meta);
        // Give the modified compass to the receiver
        receiver.getInventory().addItem(compass);
    }

    public boolean isTrackerCompass(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        return container.has(new NamespacedKey(this, "tracking"), PersistentDataType.STRING);
    }
    public void autoUpdate(){
        for (Player player: Bukkit.getOnlinePlayers()){
            ItemStack[] contents = player.getInventory().getContents();
            for (ItemStack item: contents) {
                if(item != null && item.getType() == Material.COMPASS){
                    if(isTrackerCompass(item)){
                        updateCompass(player,item,false);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerClicks(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.COMPASS) {
            if (isTrackerCompass(item)){
                updateCompass(player, player.getInventory().getItemInMainHand(),true);
            }
        }
    }

    public void updateCompass(Player player, ItemStack item, boolean announceOffline){
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        String targetName = meta.getPersistentDataContainer().get(new NamespacedKey(this, "tracking"), PersistentDataType.STRING);
        Player target = Bukkit.getPlayer(targetName);
        if (target != null){
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_GREEN + "World: " + ChatColor.GREEN + target.getWorld().getName());
            lore.add(ChatColor.YELLOW + "Location:" + ChatColor.GOLD +  Math.round(target.getLocation().getX()) + ", " + Math.round(target.getLocation().getY()) + ", " + Math.round(target.getLocation().getZ()));
            meta.setLore(lore);
            Location lodestone = target.getLocation();
            meta.setLodestoneTracked(false);
            meta.setLodestone(lodestone);
            item.setItemMeta(meta);
        } else {
            if (announceOffline)player.sendMessage(ChatColor.RED + targetName + " is not online.");
        }
    }

    public void onDisable(){
        //this function doesn't do anything rn
    }
}
