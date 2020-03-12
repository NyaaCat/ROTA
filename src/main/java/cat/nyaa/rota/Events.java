package cat.nyaa.rota;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import static cat.nyaa.rota.Utils.remindPlayer;

public class Events implements Listener {
    private ROTAPlugin plugin;

    public Events(ROTAPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChangeWOrld(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!plugin.configMain.enabledWorld.contains(world.getName())){
            return;
        }
        remindPlayer(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        World world = event.getPlayer().getWorld();
        if (!plugin.configMain.enabledWorld.contains(world.getName())){
            return;
        }
        remindPlayer(event.getPlayer());
    }

}
