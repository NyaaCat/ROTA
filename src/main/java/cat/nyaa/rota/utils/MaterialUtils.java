package cat.nyaa.rota.utils;

import cat.nyaa.rota.I18n;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class MaterialUtils {

    @SuppressWarnings("deprecation")
    public static Material getMaterial(String name, CommandSender sender) {
        Material m = Material.matchMaterial(name, false);
        if (m == null) {
            m = Material.matchMaterial(name, true);
            if (m != null) {
                m = Bukkit.getUnsafe().fromLegacy(m);
                sender.sendMessage(I18n.format("message.error.legacy_name", name, m.toString()));
            }
        }
        return m;
    }
}
