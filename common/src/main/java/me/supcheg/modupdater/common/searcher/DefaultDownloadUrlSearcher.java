package me.supcheg.modupdater.common.searcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import com.therandomlabs.curseapi.project.CurseSearchQuery;
import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.downloader.ModrinthModDownloader;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultDownloadUrlSearcher implements DownloadUrlSearcher {

    private final Updater updater;

    public DefaultDownloadUrlSearcher(@NotNull Updater updater) {
        this.updater = updater;
    }

    @NotNull
    @Override
    public Updater getUpdater() {
        return updater;
    }

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
                String slug = hits.get(0).getAsJsonObject().get("slug").getAsString();
                if (countSimilarity(slug, searching) >= 0.6) {
                    return "https://modrinth.com/mod/" + slug;
                }
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
                        if (countSimilarity(curseProject.name(), searching) >= 0.6) {
                            return curseProject.url().toString();
                        }
                    }
                }
            } catch (CurseException ignored) {
            }
        }

        return null;
    }

    private static double countSimilarity(@NotNull String s1, @NotNull String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
        }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(@NotNull String s1, @NotNull String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }
}
