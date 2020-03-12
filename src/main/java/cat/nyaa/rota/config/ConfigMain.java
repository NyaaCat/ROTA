package cat.nyaa.rota.config;

import cat.nyaa.nyaacore.Message.MessageType;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.rota.ROTAPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigMain extends PluginConfigure {
    @Serializable
    public boolean enabled = true;

    @Serializable
    public String language;

    @Serializable
    public boolean notify = true;

    @Serializable(name = "notify_interval")
    public int notifyInterval = 400;

    @Serializable(name = "notify_method")
    public MessageType notifyMethod = MessageType.CHAT;

    @Serializable
    public boolean force = false;

    @Serializable(name = "force_timeout")
    public int forceTimeout = 1200;

    @Serializable(name = "force_command")
    public String forceCommand = "kick {player} Please enable server resource pack!";

    @Serializable
    public List<String> enabledWorld = new ArrayList<>();

    @Serializable
    public boolean autoUpdate = true;
    @Serializable
    public int autoUpdateInteval = 3600 * 20;

    @StandaloneConfig
    public ResourceConfig resourceConfig = new ResourceConfig();

    @Override
    protected JavaPlugin getPlugin() {
        return ROTAPlugin.plugin;
    }
}
