package me.supcheg.modupdater.common.downloader;

import com.google.common.hash.Hashing;
import com.google.common.io.MoreFiles;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModInstance;
import me.supcheg.modupdater.common.mod.ModType;
import me.supcheg.modupdater.common.mod.SupportInfo;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import me.supcheg.modupdater.common.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ModrinthModDownloader extends ModDownloader {

    public static final String API = "https://api.modrinth.com/v2/";
    public static final String VERSION = API + "version/%s";
    public static final String PROJECT = API + "project/%s";
    public static final String SEARCH = API + "search?query=%s&limit=1";

    private final Comparator<JsonObject> fileComparator;

    public ModrinthModDownloader(@NotNull Updater updater) {
        super("Modrinth", updater);

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
            throw new IllegalStateException("An error occurred while parsing json string: " + notParsed, e);
        }
    }

    private static @NotNull JsonObject getVersion(@NotNull String id) {
        String notParsed = Util.read(VERSION.formatted(id));
        try {
            return JsonParser.parseString(notParsed).getAsJsonObject();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("An error occurred while parsing json string: " + notParsed, e);
        }
    }

    @NotNull
    @Override
    protected DownloadResult downloadLatest(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                            @NotNull DownloadConfig downloadConfig) {
        try {
            Mod mod = downloadConfig.getMod();

            String fullUrl = Objects.requireNonNull(mod.getUrl());
            String name = fullUrl.substring(fullUrl.lastIndexOf('/') + 1);

            JsonArray versionsIds = getProject(name).getAsJsonArray("versions");
            List<JsonObject> versions = new ArrayList<>(versionsIds.size());

            accessor.set("Loading versions");
            for (int i = 0; i < versionsIds.size(); i++) {
                versions.add(getVersion(versionsIds.get(i).getAsString()));
            }
            Comparator<JsonObject> comparator = updater.getVersionComparator().reversed().transform(o -> o.get("version_number").getAsString());
            versions.sort(comparator);

            for (JsonObject version : versions) {

                JsonArray gameVersions = version.getAsJsonArray("game_versions");

                JsonArray loaders = version.getAsJsonArray("loaders");

                SupportInfo info = new SupportInfo(Util.asStringList(gameVersions), ModType.fromStringCollection(Util.asStringList(loaders)));

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
                                //noinspection UnstableApiUsage
                                String localSha512 = MoreFiles.asByteSource(path).hash(Hashing.sha512()).toString();
                                if (localSha512.equals(sha512)) {
                                    Files.copy(path, savePath, StandardCopyOption.REPLACE_EXISTING);
                                    return DownloadResult.createSuccess(mod.createInstance(savePath));
                                }
                            }
                        }
                    }

                    accessor.set("Downloading file");
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
}
