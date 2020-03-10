package cat.nyaa.rota.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.rota.ROTAPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ResourceConfig extends FileConfigure {
    @Serializable
    public String url = "";

    @Serializable
    public String sha1 = "";

    @Override
    protected String getFileName() {
        return "resource.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return ROTAPlugin.plugin;
    }
}
