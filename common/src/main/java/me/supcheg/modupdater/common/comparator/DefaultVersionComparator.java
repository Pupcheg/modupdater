package me.supcheg.modupdater.common.comparator;

import me.supcheg.modupdater.common.Updater;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

public class DefaultVersionComparator extends VersionComparator {

    private final Collection<String> minecraftVersions;
    private final Pattern notNumberPattern;

    public DefaultVersionComparator(@NotNull Updater updater) {
        super(updater);
        this.notNumberPattern = Pattern.compile("[\\D\\[\\]]+");

        this.minecraftVersions = List.of(
                "1.19.1", "1.19",
                "1.18.2", "1.18.1", "1.18",
                "1.17.1", "1.17"
        );
    }

    @Override
    public int compare(String version1, String version2) {
        Objects.requireNonNull(version1, "The first version is null");
        Objects.requireNonNull(version2, "The second version is null");

        version1 = version1.trim().toLowerCase();
        version2 = version2.trim().toLowerCase();

        if (version1.equals(version2)) {
            return 0;
        }

        return asVersionInfo(version1).compareTo(asVersionInfo(version2));
    }

    @NotNull
    private VersionInfo asVersionInfo(@NotNull String versionString) {
        for (String minecraftVersion : minecraftVersions) {
            versionString = versionString.replace(minecraftVersion, "");
        }
        List<String> split = new ArrayList<>(Arrays.asList(notNumberPattern.split(versionString)));
        split.removeIf(String::isEmpty);

        if (split.size() > 3) {
            split = split.subList(split.size() - 3, split.size());
        }
        int major = asInt(split.get(0));
        int minor = split.size() > 1 ? asInt(split.get(1)) : 0;
        int maintenance = split.size() > 2 ? asInt(split.get(2)) : 0;

        return new VersionInfo(versionString, major, minor, maintenance);
    }

    private int asInt(@NotNull String version) {
        return Integer.parseInt(version.trim());
    }

    @Override
    public String toString() {
        return "DefaultVersionComparator{}";
    }

    public static class VersionInfo implements Comparable<VersionInfo> {

        private final String full;
        private final int major;
        private final int minor;
        private final int maintenance;

        private boolean isAlpha;
        private boolean isBeta;
        private boolean isRelease;

        private VersionInfo(@NotNull String full, int major, int minor, int maintenance) {
            this.full = full;
            this.major = major;
            this.minor = minor;
            this.maintenance = maintenance;
        }

        private void initAdditional() {
            this.isAlpha = full.contains(ALPHA) || full.contains(SNAPSHOT);
            this.isBeta = full.contains(BETA);
            this.isRelease = full.contains(RELEASE) || (!isAlpha && !isBeta);
        }

        @Override
        public int compareTo(@NotNull VersionInfo other) {
            return new ComparingBuilder()
                    .compare(this.major, other.major)
                    .compare(this.minor, other.minor)
                    .compare(this.maintenance, other.maintenance)
                    .supplier(() -> {
                        this.initAdditional();
                        other.initAdditional();

                        if (this.isRelease && other.isRelease) {
                            return 0;
                        } else if (this.isRelease) {
                            return 1;
                        } else if (other.isRelease) {
                            return -1;
                        } else if (this.isBeta && other.isBeta) {
                            return 0;
                        } else if (this.isBeta && other.isAlpha) {
                            return 1;
                        } else if (this.isAlpha && other.isBeta) {
                            return -1;
                        }
                        return 0;
                    })
                    .build();
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", VersionInfo.class.getSimpleName() + "[", "]")
                    .add("full='" + full + "'")
                    .add("major=" + major)
                    .add("minor=" + minor)
                    .add("maintenance=" + maintenance)
                    .add("isAlpha=" + isAlpha)
                    .add("isBeta=" + isBeta)
                    .add("isRelease=" + isRelease)
                    .toString();
        }
    }

    private static class ComparingBuilder {
        private int comparingResult;

        public @NotNull ComparingBuilder compare(int x, int y) {
            if (comparingResult == 0) {
                int c = Integer.compare(x, y);
                if (c != 0) {
                    comparingResult = c;
                }
            }
            return this;
        }

        public @NotNull ComparingBuilder supplier(@NotNull IntSupplier supplier) {
            if (comparingResult == 0) {
                int c = supplier.getAsInt();
                if (c != 0) {
                    comparingResult = c;
                }
            }
            return this;
        }

        public int build() {
            return comparingResult;
        }
    }
}
