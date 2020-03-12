package cat.nyaa.rota;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

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
        statusMap.put(event.getPlayer().getUniqueId(), PlayerStatus.valueOf(event.getStatus().name()));
    }



    enum PlayerStatus {
        UNKNOWN,
        /**
         * The resource pack has been successfully downloaded and applied to the
         * client.
         */
        SUCCESSFULLY_LOADED,
        /**
         * The client refused to accept the resource pack.
         */
        DECLINED,
        /**
         * The client accepted the pack, but download failed.
         */
        FAILED_DOWNLOAD,
        /**
         * The client accepted the pack and is beginning a download of it.
         */
        ACCEPTED;
    }
}
