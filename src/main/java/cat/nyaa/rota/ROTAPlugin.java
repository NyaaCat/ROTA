package cat.nyaa.rota;

import cat.nyaa.rota.config.ConfigMain;
import cat.nyaa.rota.utils.DownloadUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ROTAPlugin extends JavaPlugin {
    public static ROTAPlugin plugin = null;

    ConfigMain configMain;
    I18n i18n;
    AdminCommands adminCommands;
    PlayerStatusMonitor playerStatusMonitor;
    AutoRemindTask autoRemindTask;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        configMain = new ConfigMain();
        i18n = new I18n();

        configMain.load();
        i18n.load();
        i18n.setLanguage(configMain.language);
        adminCommands = new AdminCommands(this, i18n);
        playerStatusMonitor = new PlayerStatusMonitor();
        getServer().getPluginCommand("rota").setExecutor(adminCommands);
        getServer().getPluginManager().registerEvents(playerStatusMonitor, this);
        DownloadUtils.init(this);
        autoRemindTask = new AutoRemindTask();
        autoRemindTask.runTaskTimer(this, configMain.notifyInterval, configMain.notifyInterval);
    }

    public void onReload(){
        configMain = new ConfigMain();
        configMain.load();
        if (autoRemindTask != null) {
            autoRemindTask.cancel();
            autoRemindTask = new AutoRemindTask();
            autoRemindTask.runTaskTimer(this, configMain.notifyInterval, configMain.notifyInterval);
        }
        i18n.load();
        i18n.setLanguage(configMain.language);
    }
}
