package me.supcheg.modupdater.common.searcher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.mod.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.StringJoiner;

public class RemoteLibraryDownloadUrlSearcher implements DownloadUrlSearcher {

    public static final URL MAIN_LIB_URL;

    static {
        try {
            MAIN_LIB_URL = new URL("https://pastebin.com/raw/1J1rH2iN");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private final Updater updater;
    private final URL libUrl;
    private final Path downloadPath;

    private volatile JsonObject jsonObject;

    public RemoteLibraryDownloadUrlSearcher(@NotNull Updater updater) {
        this(MAIN_LIB_URL, updater);
    }

    public RemoteLibraryDownloadUrlSearcher(@NotNull URL url, @NotNull Updater updater) {
        this.libUrl = url;
        this.updater = updater;

        String urlString = url.toString();
        String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
        if (!fileName.endsWith(".json")) {
            fileName += ".json";
        }
        this.downloadPath = updater.getConfig().getDownloadFolder().resolve(fileName);
    }

    public void refresh(boolean download) throws IOException {
        if (download) {
            try (InputStream in = libUrl.openStream()) {
                Files.copy(in, downloadPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        if (Files.notExists(downloadPath)) {
            throw new FileNotFoundException();
        }

        this.jsonObject = JsonParser.parseString(Files.readString(downloadPath)).getAsJsonObject();
    }

    @Nullable
    @Override
    public String find(@NotNull Mod mod) {
        try {
            if (jsonObject == null) {
                synchronized (this) {
                    if (jsonObject == null) {
                        refresh(true);
                    }
                }
            }

            JsonElement el = jsonObject.get(mod.getId());

            if (el == null) {
                return null;
            } else if (el.isJsonArray()) {
                return el.getAsJsonArray().get(0).getAsString();
            } else {
                return el.getAsString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @NotNull
    @Override
    public Updater getUpdater() {
        return updater;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "RemoteLibraryDownloadUrlSearcher{", "}")
                .add("libUrl=" + libUrl)
                .add("downloadPath=" + downloadPath)
                .add("jsonObject=" + jsonObject)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteLibraryDownloadUrlSearcher that = (RemoteLibraryDownloadUrlSearcher) o;
        return Objects.equals(updater, that.updater) && Objects.equals(libUrl, that.libUrl)
                && Objects.equals(downloadPath, that.downloadPath) && Objects.equals(jsonObject, that.jsonObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libUrl, downloadPath, jsonObject);
    }
}