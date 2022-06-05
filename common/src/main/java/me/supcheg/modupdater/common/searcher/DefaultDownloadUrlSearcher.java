package me.supcheg.modupdater.common.searcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.CurseSearchQuery;
import me.supcheg.modupdater.common.downloader.ModrinthModDownloader;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultDownloadUrlSearcher implements DownloadUrlSearcher {

    public DefaultDownloadUrlSearcher() {}

    @Nullable
    @Override
    public String find(@NotNull Mod mod) {
        String[] searching = new String[]{mod.getName(), mod.getId()};

        String result = findModrinth(searching);
        if (result != null) {
            return result;
        }
        return findCurseForge(searching);
    }

    private @Nullable String findModrinth(@NotNull String[] searchingArray) {
        for (String searching : searchingArray) {
            JsonObject response = JsonParser.parseString(Util.read(ModrinthModDownloader.SEARCH.formatted(searching))).getAsJsonObject();
            JsonArray hits = response.getAsJsonArray("hits");
            if (hits != null && !hits.isEmpty()) {
                return "https://modrinth.com/mod/" + hits.get(0).getAsJsonObject().get("slug").getAsString();
            }
        }

        return null;
    }

    private @Nullable String findCurseForge(@NotNull String[] searchingArray) {
        for (String searching : searchingArray) {
            try {
                CurseSearchQuery query = new CurseSearchQuery()
                        .gameID(432) // MinecraftId
                        .searchFilter(searching);

                var optionalProjects = CurseAPI.searchProjects(query);
                if (optionalProjects.isPresent()) {
                    for (CurseProject curseProject : optionalProjects.get()) {
                        return curseProject.url().toString();
                    }
                }
            } catch (CurseException ignored) {
            }
        }

        return null;
    }
}
