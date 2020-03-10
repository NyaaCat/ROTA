package cat.nyaa.rota.model;

import com.google.gson.annotations.SerializedName;

public class PackMeta {
    public Pack pack;
    public static class Pack{
        @SerializedName("pack_format")
        public int packFormat;
        public String description;
    }
}
