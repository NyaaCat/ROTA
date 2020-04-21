package cat.nyaa.rota;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.rota.config.ConfigMain;
import cat.nyaa.rota.config.ResourceConfig;
import cat.nyaa.rota.model.PackMeta;
import cat.nyaa.rota.utils.DownloadUtils;
import cat.nyaa.rota.utils.MaterialUtils;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static cat.nyaa.rota.Utils.getSha1;

public class AdminCommands extends CommandReceiver {
    private final Plugin plugin;
    private final ILocalizer i18n;

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public AdminCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        this.plugin = plugin;
        i18n = _i18n;
    }

    @SubCommand(value = "option", permission = "rota.admin", tabCompleter = "optionCompleter")
    public void onOption(CommandSender sender, Arguments arguments) {
        String next = arguments.top();
        if (next != null && next.equals("resource")) {
            arguments.next();
            Class<? extends ISerializable> resourceConfigClass = ResourceConfig.class;
            setProperty(sender, resourceConfigClass, ROTAPlugin.plugin.configMain.resourceConfig, arguments);
            msg(sender, "option.set.successful");
        }
        Class<? extends ISerializable> resourceConfigClass = ConfigMain.class;
        setProperty(sender, resourceConfigClass, ROTAPlugin.plugin.configMain, arguments);
        ROTAPlugin.plugin.configMain.save();
        ROTAPlugin.plugin.onReload();
        msg(sender, "option.set.successful");
    }

    @SubCommand(value = "ignore", permission = "rota.accept")
    public void onIgnore(CommandSender sender, Arguments arguments) {
        Player player = asPlayer(sender);
        PlayerStatusMonitor.setStatus(player, PlayerStatusMonitor.PlayerStatus.SUCCESSFULLY_LOADED);
        new Message("").append(I18n.format("ignore_success")).send(player);
    }

    public List<String> optionCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        String next = arguments.top();
        if (next != null && next.equals("resource")) {
            completeStr.add("url:");
            completeStr.add("sha1:");
        } else {
            completeStr.addAll(getProperties(ConfigMain.class));
        }
        return filtered(arguments, completeStr);
    }

    @SubCommand(value = "res", permission = "rota.admin")
    public void onRes(CommandSender sender, Arguments arguments) {
        String url = arguments.top();
        if (url == null) {
            File latest = DownloadUtils.getLatest();
            if (latest == null){
                msg(sender, "res.not_available");
                return;
            }
            sendInfo(sender, latest);
        } else {
            msg(sender, "download.start");
            Utils.startDownloadTask(sender, url).async(input -> {
                sendInfo(sender, input);
                return input;
            })
                    .sync(input -> {
                        try {
                            ResourceConfig resourceConfig = ROTAPlugin.plugin.configMain.resourceConfig;
                            resourceConfig.url = url;
                            resourceConfig.sha1 = Base64.getEncoder().encodeToString(getSha1(input));
                            ROTAPlugin.plugin.configMain.save();
                        } catch (NoSuchAlgorithmException | IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).execute();
        }
    }

    private Void sendInfo(CommandSender sender, File input) {
        PackMeta packMeta = null;
        byte[] sha1 = null;
        try {
            sha1 = getSha1(input);
            ZipFile zipFile = new ZipFile(input);
            ZipEntry entry = zipFile.getEntry("pack.mcmeta");
            if (entry == null) {
                new Message(I18n.format("res.bad_pack", input.getName())).send(sender);
                return null;
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
            try {
                String temp = null;
                StringBuilder sb = new StringBuilder();
                while ((temp = bufferedReader.readLine()) != null) {
                    sb.append(temp);
                }
                packMeta = new Gson().fromJson(sb.toString(), PackMeta.class);
            }finally {
                bufferedReader.close();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (packMeta == null) {
            new Message(I18n.format("res.bad_pack", input.getName())).send(sender);
        }
        PackMeta.Pack pack = packMeta.pack;
        int packFormat = pack.packFormat;
        String versionTarget = "1.0";
        switch (packFormat) {
            case 1:
                versionTarget = "1.8.8";
                break;
            case 2:
                versionTarget = "1.9";
                break;
            case 3:
                versionTarget = "1.11";
                break;
            case 4:
                versionTarget = "1.13";
                break;
            case 5:
                versionTarget = "1.15";
                break;
        }
        String shaStr = Base64.getEncoder().encodeToString(sha1);
        msg(sender, "meta.info", versionTarget, pack.description, shaStr);
        return null;
    }

    @SubCommand(value = "status", permission = "rota.admin")
    public void onStatus(CommandSender sender, Arguments arguments) {
        Player player = arguments.nextPlayerOrSender();
        PlayerStatusMonitor.PlayerStatus status = PlayerStatusMonitor.getStatus(player);
        msg(sender, "status", player.getName(), status.name());
    }

    @SubCommand(value = "update", permission = "rota.admin")
    public void onUpdate(CommandSender sender, Arguments arguments) {
        Utils.doUpdate(sender);
    }

    @SubCommand(value = "enable", permission = "rota.admin", tabCompleter = "enableCompleter")
    public void onEnable(CommandSender sender, Arguments arguments) {
        String top = arguments.top();
        if (top == null){
            ROTAPlugin.plugin.configMain.enabled = true;
            ROTAPlugin.plugin.configMain.save();
            msg(sender, "enabled.global");
            return;
        }

        World world = Bukkit.getWorld(top);
        if (world == null){
            msg(sender, "error.invalid_world", top);
            return;
        }
        ROTAPlugin.plugin.configMain.enabledWorld.add(world.getName());
        ROTAPlugin.plugin.configMain.save();
        msg(sender, "enabled.world", world.getName());
    }

    @SubCommand(value = "disable", permission = "rota.admin", tabCompleter = "enableCompleter")
    public void onDisable(CommandSender sender, Arguments arguments) {
        String top = arguments.top();
        if (top == null){
            ROTAPlugin.plugin.configMain.enabled = false;
            ROTAPlugin.plugin.configMain.save();
            msg(sender, "disabled.global");
            return;
        }

        World world = Bukkit.getWorld(top);
        if (world == null){
            msg(sender, "error.invalid_world", top);
            return;
        }
        ROTAPlugin.plugin.configMain.enabledWorld.remove(world.getName());
        ROTAPlugin.plugin.configMain.save();
        msg(sender, "disabled.world", world.getName());
    }

    public List<String> enableCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                break;
        }
        return filtered(arguments, completeStr);
    }
    @SubCommand(value = "accept", permission = "rota.accept")
    public void onAccept(CommandSender sender, Arguments arguments) {
        Player player = asPlayer(sender);
        if (!Utils.hasValidPack()) {
            new Message(I18n.format("error.novalid_pack")).send(sender);
            return;
        }
        Utils.pushResourcePack(player);
    }

    @SubCommand(value = "reload", permission = "rota.admin")
    public void onReload(CommandSender sender, Arguments arguments){
        ROTAPlugin.plugin.onReload();
    }

    @SubCommand(value = "push", permission = "rota.admin")
    public void onPush(CommandSender sender, Arguments arguments) {
        Bukkit.getOnlinePlayers().forEach(Utils::remindPlayer);
    }

    private void setProperty(CommandSender sender, Class<? extends ISerializable> configClass, ISerializable resourceConfig, Arguments arguments) {
        String inst = arguments.nextString();
        String[] split = inst.split(":", 2);
        try {
            Field declaredField = configClass.getDeclaredField(split[0]);
            String obj = split[1];
            setUnchecked(sender, resourceConfig, declaredField, obj);
        } catch (NoSuchFieldException e) {
            throw new BadCommandException();
        }
    }


    public List<String> sampleCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                break;
        }
        return filtered(arguments, completeStr);
    }

    private static List<String> filtered(Arguments arguments, List<String> completeStr) {
        String next = arguments.at(arguments.length() - 1);
        return completeStr.stream().filter(s -> s.startsWith(next)).collect(Collectors.toList());
    }

    @Override
    public String getHelpPrefix() {
        return null;
    }


    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

    public static void setUnchecked(CommandSender sender, ISerializable power, Field field, String value) {
        I18n i18n = I18n.getInstance();
        try {
            if (value.equals("null")) {
                field.set(power, null);
                return;
            }
            field.setAccessible(true);
            if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                try {
                    field.set(power, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw new BadCommandException("internal.error.bad_int", value);
                }
            } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                try {
                    field.set(power, Long.parseLong(value));
                } catch (NumberFormatException e) {
                    throw new BadCommandException("internal.error.bad_int", value);
                }
            } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                try {
                    field.set(power, Float.parseFloat(value));
                } catch (NumberFormatException e) {
                    throw new BadCommandException("internal.error.bad_double", value);
                }
            } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                try {
                    field.set(power, Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    throw new BadCommandException("internal.error.bad_double", value);
                }
            } else if (field.getType().equals(String.class)) {
                field.set(power, value);
            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    field.set(power, Boolean.valueOf(value));
                } else {
                    throw new BadCommandException("message.error.invalid_option", value, field.getName(), "true, false");
                }
            } else if (field.getType().isEnum()) {
                try {
                    field.set(power, Enum.valueOf((Class<Enum>) field.getType(), value));
                } catch (IllegalArgumentException e) {
                    throw new BadCommandException("internal.error.bad_enum", field.getName(), Stream.of(field.getType().getEnumConstants()).map(Object::toString).collect(Collectors.joining(", ")));
                }
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> listArg = (Class<?>) listType.getActualTypeArguments()[0];
                String[] valueStrs = value.split(",");
                Stream<String> values = Arrays.stream(valueStrs).filter(s -> !s.isEmpty()).map(String::trim);
                if (field.getType().equals(List.class)) {
                    if (listArg.isEnum()) {
                        Class<? extends Enum> enumClass = (Class<? extends Enum>) listArg;
                        Stream<Enum> enumStream = values.map(v -> Enum.valueOf(enumClass, v));
                        List<Enum> list = enumStream.collect(Collectors.toList());
                        field.set(power, list);
                    } else if (listArg.equals(String.class)) {
                        List<String> list = values.collect(Collectors.toList());
                        field.set(power, list);
                    } else if (listArg.equals(Integer.class)) {
                        List<Integer> list = values.map(Integer::parseInt).collect(Collectors.toList());
                        field.set(power, list);
                    } else if (listArg.equals(Double.class)) {
                        List<Double> list = values.map(Double::parseDouble).collect(Collectors.toList());
                        field.set(power, list);
                    } else {
                        throw new BadCommandException("internal.error.command_exception");
                    }
                } else {
                    if (listArg.isEnum()) {
                        Class<? extends Enum> enumClass = (Class<? extends Enum>) listArg;
                        Stream<Enum> enumStream = values.map(v -> Enum.valueOf(enumClass, v));
                        Set<Enum> set = enumStream.collect(Collectors.toSet());
                        field.set(power, set);
                    } else if (listArg.equals(String.class)) {
                        Set<String> set = values.collect(Collectors.toSet());
                        field.set(power, set);
                    } else if (listArg.equals(Integer.class)) {
                        Set<Integer> list = values.map(Integer::parseInt).collect(Collectors.toSet());
                        field.set(power, list);
                    } else if (listArg.equals(Double.class)) {
                        Set<Double> list = values.map(Double::parseDouble).collect(Collectors.toSet());
                        field.set(power, list);
                    } else {
                        throw new BadCommandException("internal.error.command_exception");
                    }
                }
            } else if (field.getType() == ItemStack.class) {
                Material m = MaterialUtils.getMaterial(value, sender);
                ItemStack item;
                if (sender instanceof Player && value.equalsIgnoreCase("HAND")) {
                    ItemStack hand = ((Player) sender).getInventory().getItemInMainHand();
                    if (hand == null || hand.getType() == Material.AIR) {
                        throw new BadCommandException("message.error.iteminhand");
                    }
                    item = hand.clone();
                    item.setAmount(1);
                } else if (m == null || m == Material.AIR || !m.isItem()) {
                    throw new BadCommandException("message.error.material", value);
                } else {
                    item = new ItemStack(m);
                }
                field.set(power, item.clone());
            } else if (field.getType() == Enchantment.class) {
                Enchantment enchantment;
                if (VALID_KEY.matcher(value).matches()) {
                    enchantment = Enchantment.getByKey(NamespacedKey.minecraft(value));
                } else if (value.contains(":")) {
                    if (value.startsWith("minecraft:")) {
                        enchantment = Enchantment.getByKey(NamespacedKey.minecraft(value.split(":", 2)[1]));
                    } else {
                        enchantment = Enchantment.getByKey(new NamespacedKey(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(value.split(":", 2)[0])), value.split(":", 2)[1]));
                    }
                } else {
                    enchantment = Enchantment.getByName(value);
                }
                if (enchantment == null) {
                    enchantment = Arrays.stream(Enchantment.class.getDeclaredFields()).parallel().filter(f -> Modifier.isStatic(f.getModifiers())).filter(f -> f.getName().equals(value)).findAny().map(f -> {
                        try {
                            return (Enchantment) f.get(null);
                        } catch (IllegalAccessException e) {
                            throw new BadCommandException("message.error.invalid_enchant", e);
                        }
                    }).orElse(null);
                }
                field.set(power, enchantment);
            } else {
                throw new BadCommandException("message.error.invalid_command_arg");
            }
        } catch (IllegalAccessException e) {
            throw new BadCommandException("internal.error.command_exception", e);
        }
    }

    private Collection<? extends String> getProperties(Class<? extends ISerializable> configMainClass) {
        List<String> list = new ArrayList<>();
        Field[] f = configMainClass.getDeclaredFields();
        if (f.length > 0) {
            for (Field field : f) {
                ISerializable.Serializable ann = field.getAnnotation(ISerializable.Serializable.class);
                if (ann != null) {
                    list.add(field.getName() + ":");
                }
            }
        }
        Class<?> superclass = configMainClass.getSuperclass();
        if (superclass != null && superclass.isAssignableFrom(ISerializable.class)) {
            list.addAll(getProperties((Class<? extends ISerializable>) superclass));
        }
        return list;
    }

}
