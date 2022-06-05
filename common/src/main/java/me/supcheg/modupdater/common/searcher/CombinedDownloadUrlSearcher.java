package me.supcheg.modupdater.common.searcher;

import me.supcheg.modupdater.common.mod.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class CombinedDownloadUrlSearcher implements DownloadUrlSearcher {

    private final Set<DownloadUrlSearcher> downloadUrlSearcherSet;

    public CombinedDownloadUrlSearcher() {
        this.downloadUrlSearcherSet = new HashSet<>();
    }

    public boolean add(@NotNull DownloadUrlSearcher searcher) {
        return downloadUrlSearcherSet.add(searcher);
    }

    public boolean remove(@NotNull DownloadUrlSearcher searcher) {
        return downloadUrlSearcherSet.remove(searcher);
    }

    public @NotNull Set<DownloadUrlSearcher> getDownloadUrlSearchers() {
        return downloadUrlSearcherSet;
    }

    @Nullable
    @Override
    public String find(@NotNull Mod mod) {
        for (DownloadUrlSearcher downloadUrlSearcher : downloadUrlSearcherSet) {
            String result = downloadUrlSearcher.find(mod);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
