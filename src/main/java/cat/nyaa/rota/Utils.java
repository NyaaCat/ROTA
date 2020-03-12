package cat.nyaa.rota;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.rota.config.ResourceConfig;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Base64;

import static org.bukkit.util.NumberConversions.toByte;

public class Utils {

    public static void remindPlayer(Player player) {
        PlayerStatusMonitor.PlayerStatus status = PlayerStatusMonitor.getStatus(player);
        remindMessage = new TextComponent(I18n.format("remind_message"));
        new Message("").append(getRemindMessage()).send(player, ROTAPlugin.plugin.configMain.notifyMethod);
        TextComponent ignoreButton = new TextComponent(I18n.format("ignore_button"));
        ignoreButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(I18n.format("ignore_hint"))}));
        ignoreButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rota ignore"));
        new Message("").append(ignoreButton).send(player, Message.MessageType.CHAT);
    }

    public static void pushResourcePack(Player player){
        ResourceConfig resourceConfig = ROTAPlugin.plugin.configMain.resourceConfig;
        String url = resourceConfig.url;
        String sha1Str = resourceConfig.sha1;
        byte[] sha1 = Base64.getDecoder().decode(sha1Str);
        player.setResourcePack(url, sha1);
    }

    static TextComponent remindMessage = new TextComponent(I18n.format("remind_message"));
    static TextComponent ignoreButton = new TextComponent(I18n.format("ignore_button"));

    private static TextComponent getRemindMessage() {
        remindMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rota accept"));
        remindMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(I18n.format("remind_hint"))}));
        return remindMessage;
    }

    public static String toHexString(byte[] sha1) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sha1.length; i++) {
            sb.append(String.format("%2x", sha1[i]));
        }
        return sb.toString();
    }

    public static byte[] fromHexString(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }
}
