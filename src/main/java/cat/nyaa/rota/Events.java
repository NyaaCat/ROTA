package cat.nyaa.rota;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static cat.nyaa.rota.Utils.remindPlayer;

public class Events implements Listener {
    private ROTAPlugin plugin;

    public Events(ROTAPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        if (!ROTAPlugin.plugin.configMain.enabled) return;
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!plugin.configMain.enabledWorld.contains(world.getName())){
            return;
        }
        remindPlayer(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if (!ROTAPlugin.plugin.configMain.enabled) return;
        remindPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        if (!ROTAPlugin.plugin.configMain.enabled) return;
        PlayerStatusMonitor.setStatus(event.getPlayer(), PlayerStatusMonitor.PlayerStatus.UNKNOWN);
    }
}
