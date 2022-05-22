package me.supcheg.modupdater.base.comparator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

public class DefaultVersionComparator extends VersionComparator {

    private final Pattern notNumberPattern;

    public DefaultVersionComparator() {
        this.notNumberPattern = Pattern.compile("\\D");
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
        String[] split = versionString.split("\\.");
        int major = asInt(split[0]);
        int minor = split.length > 1 ? asInt(split[1]) : 0;
        int maintenance = split.length > 2 ? asInt(split[2]) : 0;

        return new VersionInfo(versionString, major, minor, maintenance);
    }

    // 1.2.0       -> 120
    // 13          -> 13
    // 0.4.1-alpha -> 41
    private int asInt(@NotNull String version) {
        try {
            return Integer.parseInt(notNumberPattern.matcher(version).replaceAll(""));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "DefaultVersionComparator{}";
    }

    public static final class VersionInfo implements Comparable<VersionInfo> {

        private final String full;
        private final int major;
        private final int minor;
        private final int maintenance;

        private boolean isAlpha;
        private boolean isBeta;
        private boolean isRelease;

        private VersionInfo(String full, int major, int minor, int maintenance) {
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
                    .add(() -> {
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
    }

    private static final class ComparingBuilder {
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

        public @NotNull ComparingBuilder add(@NotNull IntSupplier supplier) {
            if (comparingResult == 0) {
                int x = supplier.getAsInt();
                if (x != 0) {
                    comparingResult = x;
                }
            }
            return this;
        }

        public int build() {
            return comparingResult;
        }
    }
}
