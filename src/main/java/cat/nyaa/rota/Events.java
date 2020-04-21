package cat.nyaa.rota;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.rota.config.LoginAction;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import static cat.nyaa.rota.Utils.pushResourcePack;
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
        Player eventPlayer = event.getPlayer();
        class CheckTask extends BukkitRunnable{
            @Override
            public void run() {
                if (!eventPlayer.isOnline()){
                    return;
                }
                LoginAction loginAction = ROTAPlugin.plugin.configMain.loginAction;
                if (!eventPlayer.isOnGround()){
                    new CheckTask().runTaskLater(ROTAPlugin.plugin, 10);
                    return;
                }
                switch (loginAction){
                    case PUSH_AND_HINT:
                        remindPlayer(eventPlayer);
                        pushResourcePack(eventPlayer);
                        break;
                    case HINT:
                        remindPlayer(eventPlayer);
                        break;
                    case PUSH:
                        pushResourcePack(eventPlayer);
                        break;
                }
            }
        }
        new CheckTask().runTaskLater(ROTAPlugin.plugin, 10);
//        Utils.pushResourcePack(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        if (!ROTAPlugin.plugin.configMain.enabled) return;
        PlayerStatusMonitor.setStatus(event.getPlayer(), PlayerStatusMonitor.PlayerStatus.UNKNOWN);
    }
}
