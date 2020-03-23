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
    Events events;
    private UpgradeTask upgradeTask;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        configMain = new ConfigMain();
        i18n = new I18n();

        configMain.load();
        i18n.setLanguage(configMain.language);
        i18n.load();
        adminCommands = new AdminCommands(this, i18n);
        playerStatusMonitor = new PlayerStatusMonitor();
        events = new Events(this);
        getServer().getPluginCommand("rota").setExecutor(adminCommands);
        getServer().getPluginManager().registerEvents(playerStatusMonitor, this);
        getServer().getPluginManager().registerEvents(events, this);
        DownloadUtils.init(this);
        autoRemindTask = new AutoRemindTask();
        upgradeTask = new UpgradeTask();
        autoRemindTask.runTaskTimer(this, 0, configMain.notifyInterval);
        upgradeTask.runTaskTimer(this, 0, configMain.autoUpdateInteval);
    }

    @Override
    public void onDisable() {
        autoRemindTask.cancel();
        upgradeTask.cancel();
    }

    public void onReload() {
        configMain = new ConfigMain();
        configMain.load();
        if (autoRemindTask != null) {
            autoRemindTask.cancel();
            autoRemindTask = new AutoRemindTask();
            autoRemindTask.runTaskTimer(this, 0, configMain.notifyInterval);
        }
        if (upgradeTask != null) {
            upgradeTask.cancel();
            upgradeTask = new UpgradeTask();
            upgradeTask.runTaskTimer(this, 0, configMain.autoUpdateInteval);
        }
        i18n.load();
        i18n.setLanguage(configMain.language);
    }
}
