package me.supcheg.modupdater.base.network.downloaders;

import com.google.gson.*;
import me.supcheg.modupdater.base.Util;
import me.supcheg.modupdater.base.mod.Mod;
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
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class ModrinthModDownloader extends ModDownloader {

    public static final String API = "https://api.modrinth.com/v2/";
    public static final String VERSION = API + "version/%s";
    public static final String PROJECT = API + "project/%s";
    public static final String SEARCH = API + "search?query=%s&limit=1";

    // Comparators
    private final Comparator<JsonObject> reversedVersionComparator;
    private final Comparator<JsonObject> fileComparator;

    public ModrinthModDownloader() {
        super("Modrinth", "Can download mods from https://modrinth.com. Specific download data: projectId (like 'sodium' or 'appleskin').");

        this.fileComparator = (o1, o2) -> {
            boolean b1 = o1.get("primary").getAsBoolean();
            boolean b2 = o2.get("primary").getAsBoolean();

            if (b1 == b2) {
                return 0;
            }
            return b1 ? 1 : -1;
        };

        Pattern notNumberPattern = Pattern.compile("[^0-9]");
        Comparator<JsonObject> versionComparator = Comparator.comparing(jsonObject -> {
            try {
                return Integer.parseInt(notNumberPattern.matcher(jsonObject.get("version_number").getAsString()).replaceAll(""));
            } catch (Exception e) {
                return 0;
            }
        });
        this.reversedVersionComparator = versionComparator.reversed();

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
            versions.sort(reversedVersionComparator);

            for (JsonObject version : versions) {

                JsonArray gameVersions = version.getAsJsonArray("game_versions");

                JsonArray loaders = version.getAsJsonArray("loaders");

                SmallModInfo info = new SmallModInfo(Util.asList(gameVersions), ModType.fromStringCollection(Util.asList(loaders)));

                if (downloadConfig.isCorrect(info)) {
                    // Move 'primary' file(-s) to up and get
                    JsonObject fileInfo = StreamSupport.stream(version.getAsJsonArray("files").spliterator(), false)
                            .map(JsonElement::getAsJsonObject)
                            .min(fileComparator)
                            .orElseThrow(() -> new NullPointerException("No files"));

                    String fileName = fileInfo.get("filename").getAsString();
                    String url = fileInfo.get("url").getAsString();

                    try (InputStream in = new URL(url).openStream()) {
                        Path savePath = downloadConfig.getDownloadFolder().resolve(fileName);

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
