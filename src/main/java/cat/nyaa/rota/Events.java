package cat.nyaa.rota;

import cat.nyaa.nyaacore.Message;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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
        PlayerStatusMonitor.PlayerStatus status = PlayerStatusMonitor.getStatus(player);
        if (status.equals(PlayerStatusMonitor.PlayerStatus.SUCCESSFULLY_LOADED) || status.equals(PlayerStatusMonitor.PlayerStatus.ACCEPTED)) return;
        remindPlayer(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if (!ROTAPlugin.plugin.configMain.enabled) return;
        if (!Utils.hasValidPack()){
            return;
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                event.getPlayer().addPotionEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(200,5));
                Utils.pushResourcePack(event.getPlayer());
            }
        }.runTaskLater(ROTAPlugin.plugin, 10);
//        Utils.pushResourcePack(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        if (!ROTAPlugin.plugin.configMain.enabled) return;
        PlayerStatusMonitor.setStatus(event.getPlayer(), PlayerStatusMonitor.PlayerStatus.UNKNOWN);
    }
}
