package me.supcheg.modupdater.common.comparator;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.util.UpdaterHolder;
import me.supcheg.modupdater.common.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public abstract class VersionComparator implements Comparator<String>, UpdaterHolder {

    public static @NotNull VersionComparator from(@NotNull Updater updater, @NotNull Comparator<String> comparator) {
        if (comparator instanceof VersionComparator versionComparator) {
            Util.validateSameUpdater(updater, versionComparator);
            return versionComparator;
        } else {
            return new SimpleVersionComparator(updater, comparator);
        }
    }

    public static final String ALPHA = "alpha";
    public static final String SNAPSHOT = "snapshot";
    public static final String BETA = "beta";
    public static final String RELEASE = "release";

    private final Updater updater;
    private volatile VersionComparator reserved;

    public VersionComparator(@NotNull Updater updater) {
        this.updater = updater;
    }

    @NotNull
    @Override
    public Updater getUpdater() {
        return updater;
    }

    @Override
    public abstract int compare(String version1, String version2);

    public @NotNull <T> Comparator<T> transform(@NotNull Function<T, String> function) {
        return new Transforming<>(function);
    }

    @Override
    public VersionComparator reversed() {
        if (reserved == null) {
            synchronized (this) {
                if (reserved == null) {
                    reserved = new Reserved(updater);
                }
            }
        }
        return reserved;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{}";
    }

    private class Transforming<T> implements Comparator<T> {

        private final Function<T, String> function;

        public Transforming(@NotNull Function<T, String> function) {
            this.function = function;
        }

        @Override
        public int compare(T o1, T o2) {
            Objects.requireNonNull(o1, "The first object is null");
            Objects.requireNonNull(o2, "The second object is null");

            String ver1 = Objects.requireNonNull(function.apply(o1), "The function returns null from the first object");
            String ver2 = Objects.requireNonNull(function.apply(o2), "The function returns null from the second object");

            return VersionComparator.this.compare(ver1, ver2);
        }
    }

    private class Reserved extends VersionComparator {

        public Reserved(@NotNull Updater updater) {
            super(updater);
        }

        @Override
        public int compare(String v1, String v2) {
            return VersionComparator.this.compare(v2, v1);
        }

        @Override
        public VersionComparator reversed() {
            return VersionComparator.this;
        }

        @Override
        public String toString() {
            return "Reserved" + VersionComparator.this;
        }
    }
}
