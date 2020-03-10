package cat.nyaa.rota;

import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.plugin.Plugin;

public class I18n extends LanguageRepository {
    private static I18n INSTANCE;

    String language = "en_US";

    I18n() {
        INSTANCE = this;
    }

    public static String format(String template, Object ... args){
        return INSTANCE.getFormatted(template, args);
    }

    public static I18n getInstance() {
        return INSTANCE;
    }

    @Override
    protected Plugin getPlugin() {
        return ROTAPlugin.plugin;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    protected String getLanguage() {
        return language;
    }
}
