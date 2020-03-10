package cat.nyaa.rota.utils;

import cat.nyaa.rota.ROTAPlugin;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.stream.Stream;

public class DownloadUtils {
    private static File parent;
    private static File dataFolder;

    private static OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .build();
    }

    public static void init(ROTAPlugin rotaPlugin) {
        dataFolder = rotaPlugin.getDataFolder();
        ensureParent();
    }

    public static File download(String url, String fileName) throws IOException {
        File f = new File(parent, fileName);
        Request build = new Request.Builder()
                .get()
                .url(url)
                .build();
        Response execute = client.newCall(build).execute();
        ResponseBody body = execute.body();
        if (body == null) return null;
        InputStream inputStream = body.byteStream();
        FileOutputStream fileOutputStream = new FileOutputStream(f, false);
        try {
            int temp;
            byte[] buff = new byte[1024];
            while ((temp = inputStream.read(buff)) != -1) {
                fileOutputStream.write(buff, 0, temp);
                fileOutputStream.flush();
            }
        }finally {
            inputStream.close();
            fileOutputStream.close();
        }
        return f;
    }

    public static File getLatest() {
        ensureParent();
        File[] values = parent.listFiles();
        if (values == null)
            return null;
        File f = Stream.of(values)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
        return f;
    }

    private static void ensureParent() {
        parent = new File(dataFolder, "downloads");
        if (!parent.exists()) {
            parent.mkdirs();
        }
    }
}
