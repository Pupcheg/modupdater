package me.supcheg.modupdater.base.network.downloaders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.CurseSearchQuery;
import me.supcheg.modupdater.base.Updater;
import me.supcheg.modupdater.base.Util;
import me.supcheg.modupdater.base.mod.Mod;
import me.supcheg.modupdater.base.network.DownloadConfig;
import me.supcheg.modupdater.base.network.DownloadResult;
import me.supcheg.modupdater.base.network.ModDownloader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchingModDownloader extends ModDownloader {
    private static final String MODRINTH_MOD = "https://modrinth.com/mod/%s";

    private final Updater updater;

    public SearchingModDownloader(@NotNull Updater updater) {
        super("Searching", "Trying to find a mod on Modrinth or CurseForge.");
        this.updater = updater;
    }

    @Override
    public boolean canDownload(@NotNull String url) {
        return true;
    }

    @NotNull
    @Override
    public DownloadResult downloadLatest(@NotNull DownloadConfig downloadConfig) {
        Mod mod = downloadConfig.getMod();

        String name = mod.getName();
        try {
            String url = MODRINTH_MOD.formatted(name);
            // If mod doesn't exist, site will return an error page with this text
            boolean exists = !Util.read(url).contains("Project not found");
            if (exists) {
                mod.setUrl(url);
                updater.getConfig().setCustomUrl(mod, url);
                return mod.getDownloader().downloadLatest(downloadConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject response = JsonParser.parseString(Util.read(ModrinthModDownloader.SEARCH.formatted(name))).getAsJsonObject();

        DownloadResult modrinthDownloadResult = null;
        for (JsonElement el : response.getAsJsonArray("hits")) {
            JsonObject project = el.getAsJsonObject();

            String slug = project.get("slug").getAsString();

            if (slug.equalsIgnoreCase(name) || project.get("title").getAsString().equalsIgnoreCase(name)) {
                // Full match
                String url = MODRINTH_MOD.formatted(slug);
                ModDownloader modrinth = updater.findModDownloaderForUrl(url);
                if (modrinth instanceof SearchingModDownloader) {
                    // SearchingModDownloader
                    // It must be an error
                    // Return unsuccessful DownloadResult, because it will throw StackOverFlow
                    return DownloadResult.createError(
                            "Updater#findModDownloaderForUrl(String) returns SearchingModDownloader instance for " + url,
                            "Used Updater instance: " + updater
                    );
                }

                String oldUrl = mod.getUrl();
                mod.setUrlWithOutChecks(url);
                DownloadResult result = modrinth.downloadLatest(downloadConfig);

                if (result.isSuccess()) {
                    // Founded url is working fine
                    mod.setDownloader(modrinth);
                    updater.getConfig().setCustomUrl(mod, url);
                    return result;
                }
                // Not working, try CurseForge
                mod.setUrlWithOutChecks(oldUrl);
                modrinthDownloadResult = result;
                break;
            }
        }

        DownloadResult curseForgeDownloadResult = null;
        try {
            CurseSearchQuery query = new CurseSearchQuery()
                    .gameID(432) // MinecraftId
                    .searchFilter(name);

            var optionalProjects = CurseAPI.searchProjects(query);
            if (optionalProjects.isPresent()) {
                for (CurseProject curseProject : optionalProjects.get()) {
                    String url = curseProject.url().toString();

                    ModDownloader curseForge = updater.findModDownloaderByName("curseforge");
                    if (curseForge == null) {
                        return DownloadResult.createError("CurseForgeModDownloader is null.",
                                "Used Updater instance: " + updater
                        );
                    }
                    if (curseForge instanceof SearchingModDownloader) {
                        // SearchingModDownloader
                        // It must be an error
                        // Return unsuccessful DownloadResult, because it will throw StackOverFlow
                        return DownloadResult.createError(
                                "Updater#findModDownloaderByName(String) returns SearchingModDownloader instance with name: 'curseforge'.",
                                "Used Updater instance: " + updater
                        );
                    }

                    String oldUrl = mod.getUrl();
                    mod.setUrlWithOutChecks(url);
                    DownloadResult result = curseForge.downloadLatest(downloadConfig);
                    if (result.isSuccess()) {
                        // Founded url is working fine
                        mod.setDownloader(curseForge);
                        updater.getConfig().setCustomUrl(mod, url);
                        return result;
                    }
                    // Not working, try CurseForge
                    mod.setUrlWithOutChecks(oldUrl);
                    curseForgeDownloadResult = result;
                    break;
                }
            }
            // No matches
        } catch (CurseException ignored) {
        }

        // Check if the standard downloader is working fine
        ModDownloader standard = updater.findModDownloaderForUrl(mod.getUrl());
        boolean alwaysTrue = standard.canDownload("");
        if (!alwaysTrue && !(standard instanceof CurseForgeModDownloader) && !(standard instanceof ModrinthModDownloader)) {
            DownloadResult result = standard.downloadLatest(downloadConfig);
            if (result.isSuccess()) {
                mod.setDownloader(standard);
                // Yes, the standard downloader is working fine
                return result;
            }
        }

        // Failed
        mod.setDownloader(updater.findModDownloaderByName("unknown"));

        List<String> message = new ArrayList<>(9);
        message.addAll(Arrays.asList(
                "It was not possible to find a suitable loader.",
                "Modrinth result:"
        ));
        if (modrinthDownloadResult != null) {
            message.addAll(Arrays.asList(modrinthDownloadResult.getMessage()));
        } else {
            message.add("No project was found.");
        }
        message.add("CurseForge result:");
        if (curseForgeDownloadResult != null) {
            message.addAll(Arrays.asList(curseForgeDownloadResult.getMessage()));
        } else {
            message.add("No project was found.");
        }

        return DownloadResult.createError(message.toArray(String[]::new));
    }
}
