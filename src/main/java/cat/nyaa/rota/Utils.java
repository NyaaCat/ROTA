package cat.nyaa.rota;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.rota.config.ResourceConfig;
import cat.nyaa.rota.utils.DownloadUtils;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

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

    public static void doUpdate(CommandSender sender) {

        ResourceConfig resourceConfig = ROTAPlugin.plugin.configMain.resourceConfig;
        String url = resourceConfig.url;
        String sha1Str = resourceConfig.sha1;
        new Message(I18n.format("download.start")).send(sender);
        startDownloadTask(sender, url).async((input) -> {
            new Message(I18n.format("download.complete")).send(sender);
            try {
                byte[] sha1 = getSha1(input);
                if (sha1Str.equals(Base64.getEncoder().encodeToString(sha1))){
                    new Message(I18n.format("update.no_update")).send(sender);
                    return null;
                }else {
                    new Message("update.success");
                    return input;
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).abortIfNull()
                .sync((input -> {
                    Bukkit.getOnlinePlayers().forEach(player ->{
                        Utils.remindPlayer(player);
                    });
                    return input;
                })) .sync(input -> {
            try {
                resourceConfig.url = url;
                resourceConfig.sha1 = Base64.getEncoder().encodeToString(getSha1(input));
                ROTAPlugin.plugin.configMain.save();
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }).execute();
    }

    public static byte[] getSha1(File input) throws NoSuchAlgorithmException, IOException {
        FileInputStream fileInputStream = new FileInputStream(input);
        MessageDigest instance = MessageDigest.getInstance("SHA-1");
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = fileInputStream.read(buff)) != -1) {
            instance.update(buff, 0, len);
        }
        return instance.digest();
    }

    public static TaskChain<File> startDownloadTask(CommandSender sender, String url) {
        return BukkitTaskChainFactory.create(ROTAPlugin.plugin).newChain()
                .async(input -> {
                    if (url.equals("") || url.lastIndexOf("/") == -1){
                        new Message(I18n.format("error.invalid_url", url)).send(sender);
                        return null;
                    }
                    String fileName = url.substring(url.lastIndexOf("/"));
                    try {
                        return DownloadUtils.download(url, fileName);
                    } catch (IOException e) {
                        new Message(I18n.format("res.http_fail")).send(sender);
                        e.printStackTrace();
                        return null;
                    }
                })
                .abortIf(Objects::isNull);
    }

    public static boolean validate(File latest, String sha1) {
        if (latest == null){
            return false;
        }
        try {
            return Base64.getEncoder().encodeToString(getSha1(latest)).equals(sha1);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasValidPack() {
        File latest = DownloadUtils.getLatest();
        String sha1 = ROTAPlugin.plugin.configMain.resourceConfig.sha1;
        return validate(latest, sha1);
    }
}
