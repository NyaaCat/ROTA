package cat.nyaa.rota;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AutoRemindTask extends BukkitRunnable {
    @Override
    public void run() {
        if (!ROTAPlugin.plugin.configMain.enabled) return;
        if (!ROTAPlugin.plugin.configMain.notify) return;

        List<String> enabledWorld = ROTAPlugin.plugin.configMain.enabledWorld;
        enabledWorld.forEach(s ->{
            World world = Bukkit.getWorld(s);
            if (world != null){
                world.getPlayers().forEach(player -> {
                    PlayerStatusMonitor.PlayerStatus status = PlayerStatusMonitor.getStatus(player);
                    if (status.equals(PlayerStatusMonitor.PlayerStatus.SUCCESSFULLY_LOADED) || status.equals(PlayerStatusMonitor.PlayerStatus.ACCEPTED)) return;
                    Utils.remindPlayer(player);
                });
            }
        });
    }
}
