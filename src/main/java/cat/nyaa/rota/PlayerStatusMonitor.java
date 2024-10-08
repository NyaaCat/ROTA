package cat.nyaa.rota;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatusMonitor implements Listener {
    private static Map<UUID, PlayerStatus> statusMap = new HashMap<>();

    public static PlayerStatus getStatus(Player player){
        return getStatus(player.getUniqueId());
    }

    public static PlayerStatus getStatus(UUID uniqueId) {
        return statusMap.computeIfAbsent(uniqueId, uuid -> PlayerStatus.UNKNOWN);
    }

    public static void setStatus(Player player, PlayerStatus successfullyLoaded) {
        statusMap.put(player.getUniqueId(), successfullyLoaded);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStatusChange(PlayerResourcePackStatusEvent event){
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        statusMap.put(event.getPlayer().getUniqueId(), PlayerStatus.valueOf(status.name()));
        if (status.equals(PlayerResourcePackStatusEvent.Status.ACCEPTED)){
            event.getPlayer().addPotionEffect(PotionEffectType.RESISTANCE.createEffect(400,5));
        }else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getPlayer().hasPotionEffect(PotionEffectType.RESISTANCE)) {
                        event.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
                    }
                }
            }.runTaskLater(ROTAPlugin.plugin, 60);
        }
    }



    enum PlayerStatus {
        UNKNOWN,
        // https://jd.papermc.io/paper/1.21.1/org/bukkit/event/player/PlayerResourcePackStatusEvent.Status.html
        ACCEPTED,
        //The client accepted the pack and is beginning a download of it.
        DECLINED,
        //The client refused to accept the resource pack.
        DISCARDED,
        //The pack was discarded by the client.
        DOWNLOADED,
        //The client successfully downloaded the pack.
        FAILED_DOWNLOAD,
        //The client accepted the pack, but download failed.
        FAILED_RELOAD,
        //The client was unable to reload the pack.
        INVALID_URL,
        //The pack URL was invalid.
        SUCCESSFULLY_LOADED
        //The resource pack has been successfully downloaded and applied to the client.
    }
}
