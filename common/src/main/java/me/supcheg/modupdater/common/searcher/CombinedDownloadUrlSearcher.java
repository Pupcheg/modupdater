package me.supcheg.modupdater.common.searcher;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Objects;

public class CombinedDownloadUrlSearcher extends ArrayDeque<DownloadUrlSearcher> implements DownloadUrlSearcher {

    private final Updater updater;

    public CombinedDownloadUrlSearcher(@NotNull Updater updater, @NotNull Collection<DownloadUrlSearcher> searchers) {
        super(searchers);
        this.updater = updater;
    }

    public CombinedDownloadUrlSearcher(@NotNull Updater updater) {
        super();
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
        Util.validateSameUpdater(this, mod);
        for (DownloadUrlSearcher downloadUrlSearcher : this) {
            String result = downloadUrlSearcher.find(mod);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public void addFirst(@NotNull DownloadUrlSearcher searcher) {
        Util.validateSameUpdater(this, searcher);
        super.addFirst(searcher);
    }

    @Override
    public void addLast(@NotNull DownloadUrlSearcher searcher) {
        Util.validateSameUpdater(this, searcher);
        super.addLast(searcher);
    }

    @Override
    public String toString() {
        return "CombinedDownloadUrlSearcher[" + super.toString() + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombinedDownloadUrlSearcher that = (CombinedDownloadUrlSearcher) o;
        return Objects.equals(updater, that.updater) && super.equals(o);
    }
}
