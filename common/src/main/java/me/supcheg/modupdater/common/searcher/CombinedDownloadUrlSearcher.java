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

    private Updater updater;

    public CombinedDownloadUrlSearcher(@NotNull Collection<DownloadUrlSearcher> searchers) {
        super(searchers);
    }

    public CombinedDownloadUrlSearcher() {
        super();
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
        for (DownloadUrlSearcher downloadUrlSearcher : this) {
            String result = downloadUrlSearcher.find(mod);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private void validate(@NotNull DownloadUrlSearcher searcher) {
        if (CombinedDownloadUrlSearcher.this.updater == null) {
            CombinedDownloadUrlSearcher.this.updater = searcher.getUpdater();
        } else {
            Util.validateSameUpdater(CombinedDownloadUrlSearcher.this, searcher);
        }
    }

    @Override
    public void addFirst(@NotNull DownloadUrlSearcher searcher) {
        validate(searcher);
        super.addFirst(searcher);
    }

    @Override
    public void addLast(@NotNull DownloadUrlSearcher searcher) {
        validate(searcher);
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

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
