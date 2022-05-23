package me.supcheg.modupdater.base.network.downloaders;

import com.google.common.hash.Hashing;
import com.google.common.io.MoreFiles;
import com.google.gson.*;
import me.supcheg.modupdater.base.Updater;
import me.supcheg.modupdater.base.Util;
import me.supcheg.modupdater.base.mod.Mod;
import me.supcheg.modupdater.base.mod.ModInstance;
import me.supcheg.modupdater.base.mod.ModType;
import me.supcheg.modupdater.base.mod.SmallModInfo;
import me.supcheg.modupdater.base.network.DownloadConfig;
import me.supcheg.modupdater.base.network.DownloadResult;
import me.supcheg.modupdater.base.network.ModDownloader;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

public class ModrinthModDownloader extends ModDownloader {

    public static final String API = "https://api.modrinth.com/v2/";
    public static final String VERSION = API + "version/%s";
    public static final String PROJECT = API + "project/%s";
    public static final String SEARCH = API + "search?query=%s&limit=1";

    private final Comparator<JsonObject> fileComparator;

    public ModrinthModDownloader(@NotNull Updater updater) {
        super("Modrinth", "Can download mods from https://modrinth.com. Specific download data: projectId (like 'sodium' or 'appleskin').", updater);

        this.fileComparator = (o1, o2) -> {
            boolean b1 = o1.get("primary").getAsBoolean();
            boolean b2 = o2.get("primary").getAsBoolean();

            if (b1 == b2) {
                return 0;
            }
            return b1 ? 1 : -1;
        };
    }

    private static @NotNull JsonObject getProject(@NotNull String nameOrId) {
        String notParsed = Util.read(PROJECT.formatted(nameOrId));
        try {
            return JsonParser.parseString(notParsed).getAsJsonObject();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Project: " + notParsed, e);
        }
    }

    private static @NotNull JsonObject getVersion(@NotNull String id) {
        String notParsed = Util.read(VERSION.formatted(id));
        try {
            return JsonParser.parseString(notParsed).getAsJsonObject();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Version: " + notParsed, e);
        }
    }

    @NotNull
    @Override
    public DownloadResult downloadLatest(@NotNull DownloadConfig downloadConfig) {
        try {
            Mod mod = downloadConfig.getMod();

            String name;
            if (mod.getSpecificDownloadData() == null) {
                String fullUrl = mod.getUrl();
                name = fullUrl.substring(fullUrl.lastIndexOf('/') + 1);
            } else {
                name = mod.getSpecificDownloadData();
            }

            JsonArray versionsIds = getProject(name).getAsJsonArray("versions");
            List<JsonObject> versions = new ArrayList<>(versionsIds.size());

            for (int i = 0; i < versionsIds.size(); i++) {
                versions.add(getVersion(versionsIds.get(i).getAsString()));
            }
            Comparator<JsonObject> comparator = updater.getVersionComparator().transform(o -> o.get("version_number").getAsString());
            versions.sort(comparator);

            for (JsonObject version : versions) {

                JsonArray gameVersions = version.getAsJsonArray("game_versions");

                JsonArray loaders = version.getAsJsonArray("loaders");

                SmallModInfo info = new SmallModInfo(Util.asList(gameVersions), ModType.fromStringCollection(Util.asList(loaders)));

                if (downloadConfig.isCorrect(info)) {
                    // Move 'primary' file(-s) to up and get
                    JsonObject fileInfo = Util.stream(version.getAsJsonArray("files"))
                            .map(JsonElement::getAsJsonObject)
                            .max(fileComparator)
                            .orElseThrow(() -> new NullPointerException("No files"));

                    String fileName = fileInfo.get("filename").getAsString();
                    String url = fileInfo.get("url").getAsString();

                    JsonObject hashes = fileInfo.getAsJsonObject("hashes");

                    Path savePath = downloadConfig.getDownloadFolder().resolve(fileName);

                    if (hashes != null) {
                        JsonElement sha512el = hashes.get("sha512");
                        if (sha512el != null) {
                            // File has sha512 hash, check if we have downloaded the same
                            String sha512 = sha512el.getAsString();
                            for (ModInstance instance : mod.getInstances()) {
                                Path path = instance.getPath();
                                String localSha512 = MoreFiles.asByteSource(path).hash(Hashing.sha512()).toString();
                                if (localSha512.equals(sha512)) {
                                    Files.copy(path, savePath, StandardCopyOption.REPLACE_EXISTING);
                                    return DownloadResult.createSuccess(mod.createInstance(savePath));
                                }
                            }
                        }
                    }

                    try (InputStream in = new URL(url).openStream()) {
                        Files.copy(in, savePath, StandardCopyOption.REPLACE_EXISTING);

                        return DownloadResult.createSuccess(mod.createInstance(savePath));
                    }
                }
            }
            return DownloadResult.createError("No valid versions");
        } catch (Exception e) {
            return DownloadResult.createError(e);
        }
    }

    @Override
    public boolean isAlwaysTrue() {
        return false;
    }
}
