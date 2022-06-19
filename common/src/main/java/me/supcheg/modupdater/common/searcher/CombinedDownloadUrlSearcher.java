package me.supcheg.modupdater.common.searcher;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CombinedDownloadUrlSearcher implements DownloadUrlSearcher {

    private Updater updater;
    private final Set<DownloadUrlSearcher> downloadUrlSearcherSet;

    public CombinedDownloadUrlSearcher() {
        this.downloadUrlSearcherSet = new HashSet<>();
    }

    public boolean add(@NotNull DownloadUrlSearcher searcher) {
        if (this.updater == null) {
            this.updater = searcher.getUpdater();
        } else {
            Util.validateSameUpdater(this, searcher);
        }
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
    public Updater getUpdater() {
        return updater;
    }

    @Nullable
    @Override
    public String find(@NotNull Mod mod) {
        Util.validateSameUpdater(this, mod);
        for (DownloadUrlSearcher downloadUrlSearcher : downloadUrlSearcherSet) {
            String result = downloadUrlSearcher.find(mod);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "CombinedDownloadUrlSearcher{downloaders=" + downloadUrlSearcherSet + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombinedDownloadUrlSearcher that = (CombinedDownloadUrlSearcher) o;
        return Objects.equals(updater, that.updater) && Objects.equals(downloadUrlSearcherSet, that.downloadUrlSearcherSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updater, downloadUrlSearcherSet);
    }
}
