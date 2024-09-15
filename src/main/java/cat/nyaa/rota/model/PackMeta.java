package cat.nyaa.rota.model;

import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.text.Component;

public class PackMeta {
    public Pack pack;
    public static class Pack{
        @SerializedName("pack_format")
        public int packFormat;
        public Component description;
    }
}
