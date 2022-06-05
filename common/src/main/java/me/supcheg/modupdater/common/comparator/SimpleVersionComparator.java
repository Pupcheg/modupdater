package me.supcheg.modupdater.common.comparator;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;

public class SimpleVersionComparator extends VersionComparator {

    private final Comparator<String> comparator;

    public SimpleVersionComparator(@NotNull Comparator<String> comparator) {
        Objects.requireNonNull(comparator, "Comparator is null");
        this.comparator = comparator;
    }

    @Override
    public int compare(String version1, String version2) {
        Objects.requireNonNull(version1, "The first version is null");
        Objects.requireNonNull(version2, "The second version is null");

        return comparator.compare(version1, version2);
    }

    @Override
    public String toString() {
        return "SimpleVersionComparator{comparator=%s}".formatted(comparator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleVersionComparator that = (SimpleVersionComparator) o;
        return Objects.equals(comparator, that.comparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comparator);
    }
}
