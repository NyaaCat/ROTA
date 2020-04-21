package cat.nyaa.rota;

import cat.nyaa.rota.config.LoginAction;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoRemindTask extends BukkitRunnable {
    @Override
    public void run() {
        if (!ROTAPlugin.plugin.configMain.enabled) return;
        if (!ROTAPlugin.plugin.configMain.notify) return;
        if (!Utils.hasValidPack())return;
        LoginAction loginAction = ROTAPlugin.plugin.configMain.loginAction;
        if (!loginAction.equals(LoginAction.HINT) && !loginAction.equals(LoginAction.PUSH_AND_HINT)){
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerStatusMonitor.PlayerStatus status = PlayerStatusMonitor.getStatus(player);
            if (status.equals(PlayerStatusMonitor.PlayerStatus.SUCCESSFULLY_LOADED) || status.equals(PlayerStatusMonitor.PlayerStatus.ACCEPTED)) return;
            Utils.remindPlayer(player);
        });
    }
}
