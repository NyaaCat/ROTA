package cat.nyaa.rota;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class UpgradeTask extends BukkitRunnable {
    @Override
    public void run() {
        if (!ROTAPlugin.plugin.configMain.autoUpdate) {
            return;
        }

        Utils.doUpdate(Bukkit.getConsoleSender());
    }
}
