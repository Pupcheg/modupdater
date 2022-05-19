package me.supcheg.modupdater.base.comparator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

public class DefaultVersionComparator extends VersionComparator {

    private final Pattern numberPattern;
    private final Pattern notNumberPattern;

    public DefaultVersionComparator() {
        this.numberPattern = Pattern.compile("\\d");
        this.notNumberPattern = Pattern.compile("\\D");
    }

    @Override
    public int compare(String version1string, String version2string) {
        Objects.requireNonNull(version1string, "The first version is null");
        Objects.requireNonNull(version2string, "The second version is null");

        version1string = version1string.trim().toLowerCase();
        version2string = version2string.trim().toLowerCase();

        if (version1string.equals(version2string)) {
            return 0;
        }

        if (numberPattern.matcher(version1string).matches() && numberPattern.matcher(version2string).matches()) {
            System.out.println(version1string + " and " + version2string + " are numbers");
            return Integer.compare(asInt(version1string), asInt(version2string));
        }

        int ver1length = version1string.length();
        int ver2length = version2string.length();

        if (ver1length != ver2length &&
                // Check is version string ends with not-number. Like 'x.x.x-release'
                numberPattern.matcher(version1string.substring(ver1length - 1)).matches() &&
                numberPattern.matcher(version2string.substring(ver2length - 1)).matches()) {
            if (ver1length > ver2length) {
                version2string = append(version2string, ver1length);
            } else {
                version1string = append(version1string, ver2length);
            }
        }

        VersionInfo version1 = new VersionInfo(version1string);
        VersionInfo version2 = new VersionInfo(version2string);

        int comparedInts = Integer.compare(version1.intValue, version2.intValue);

        if (comparedInts == 0) {
            version1.initAdditional();
            version2.initAdditional();

            if (version1.isRelease && version2.isRelease) {
                return 0;
            } else if (version1.isRelease) {
                return 1;
            } else if (version2.isRelease) {
                return -1;
            } else if (version1.isBeta && version2.isBeta) {
                return 0;
            } else if (version1.isBeta && version2.isAlpha) {
                return 1;
            } else if (version1.isAlpha && version2.isBeta) {
                return -1;
            }
            return 0;

        } else {
            return comparedInts;
        }
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

    @NotNull
    private String append(@NotNull String string, int expected) {
        int ver1length = string.length();
        StringBuilder version1stringBuilder = new StringBuilder(string);
        do {
            version1stringBuilder.append(".0");
            ver1length += 2;
        } while (expected > ver1length);
        return version1stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "DefaultVersionComparator{}";
    }

    private final class VersionInfo {

        private final String stringValue;
        private final int intValue;
        private boolean isAlpha;
        private boolean isBeta;
        private boolean isRelease;

        private VersionInfo(String stringValue) {
            this.stringValue = stringValue;
            this.intValue = asInt(stringValue);
        }

        private void initAdditional() {
            this.isAlpha = stringValue.contains(ALPHA) || stringValue.contains(SNAPSHOT);
            this.isBeta = stringValue.contains(BETA);
            this.isRelease = stringValue.contains(RELEASE) || (!isAlpha && !isBeta);
        }

    }

}
