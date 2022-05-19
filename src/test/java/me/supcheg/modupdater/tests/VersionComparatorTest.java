package me.supcheg.modupdater.tests;

import com.google.gson.JsonObject;
import me.supcheg.modupdater.base.comparator.DefaultVersionComparator;
import me.supcheg.modupdater.base.comparator.VersionComparator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"ResultOfMethodCallIgnored", "EqualsWithItself"})
public class VersionComparatorTest {

    @Test
    public void run() {
        testAll(new DefaultVersionComparator());
    }

    public void testAll(VersionComparator comparator) {
        testNormal(comparator);
        testReserved(comparator);
        testTransforming(comparator);
    }

    public void testNormal(VersionComparator comparator) {
        assertThrows(NullPointerException.class, () -> comparator.compare(null, "3.0.0"));
        assertThrows(NullPointerException.class, () -> comparator.compare("3.0.0", null));
        assertThrows(NullPointerException.class, () -> comparator.compare(null, null));

        assertResult(comparator, "1", "2", ComparingResult.LOWER);
        assertResult(comparator, "2", "1", ComparingResult.UPPER);
        assertResult(comparator, "2", "2", ComparingResult.EQUALS);
        assertResult(comparator, "10", "1", ComparingResult.UPPER);

        assertResult(comparator, "2.3", "1.0.1", ComparingResult.UPPER);
        assertResult(comparator, "3.10.1", "4.0.0", ComparingResult.LOWER);

        assertResult(comparator, "3.0.0-alpha", "3.0.0-beta", ComparingResult.LOWER);
        assertResult(comparator, "3.0.0", "3.0.0-beta", ComparingResult.UPPER);

        assertResult(comparator, "3.0.0-beta", "3.0.0-release", ComparingResult.LOWER);
        assertResult(comparator, "3.0.0", "3.0", ComparingResult.EQUALS);
    }


    public void testReserved(VersionComparator notReversedComparator) {
        VersionComparator comparator = notReversedComparator.reversed();
        assertThrows(NullPointerException.class, () -> comparator.compare(null, "3.0.0"));
        assertThrows(NullPointerException.class, () -> comparator.compare("3.0.0", null));
        assertThrows(NullPointerException.class, () -> comparator.compare(null, null));

        assertResult(comparator, "1", "2", ComparingResult.UPPER);
        assertResult(comparator, "2", "1", ComparingResult.LOWER);
        assertResult(comparator, "2", "2", ComparingResult.EQUALS);
        assertResult(comparator, "10", "1", ComparingResult.LOWER);

        assertResult(comparator, "2.3", "1.0.1", ComparingResult.LOWER);
        assertResult(comparator, "3.10.1", "4.0.0", ComparingResult.UPPER);

        assertResult(comparator, "3.0.0-alpha", "3.0.0-beta", ComparingResult.UPPER);
        assertResult(comparator, "3.0.0", "3.0.0-beta", ComparingResult.LOWER);

        assertResult(comparator, "3.0.0-beta", "3.0.0-release", ComparingResult.UPPER);
        assertResult(comparator, "3.0.0", "3.0", ComparingResult.EQUALS);
    }

    public void testTransforming(@NotNull VersionComparator notTransformedComparator) {
        Function<JsonObject, String> function = o -> o.get("version").getAsString();
        Comparator<JsonObject> comparator = notTransformedComparator.transform(function);

        assertResult(comparator, "1", "2", ComparingResult.LOWER);
        assertResult(comparator, "2", "1", ComparingResult.UPPER);
        assertResult(comparator, "2", "2", ComparingResult.EQUALS);
        assertResult(comparator, "10", "1", ComparingResult.UPPER);

        assertResult(comparator, "2.3", "1.0.1", ComparingResult.UPPER);
        assertResult(comparator, "3.10.1", "4.0.0", ComparingResult.LOWER);

        assertResult(comparator, "3.0.0-alpha", "3.0.0-beta", ComparingResult.LOWER);
        assertResult(comparator, "3.0.0", "3.0.0-beta", ComparingResult.UPPER);

        assertResult(comparator, "3.0.0-beta", "3.0.0-release", ComparingResult.LOWER);
        assertResult(comparator, "3.0.0", "3.0", ComparingResult.EQUALS);
    }

    public void assertResult(@NotNull Comparator<JsonObject> comparator, @NotNull String ver1, @NotNull String ver2, @NotNull ComparingResult expectedResult) {
        assertResult(comparator, create(ver1), create(ver2), expectedResult);
    }

    public static @NotNull JsonObject create(String version) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("version", version);
        return jsonObject;
    }

    public <T> void assertResult(@NotNull Comparator<T> comparator, @NotNull T ver1, @NotNull T ver2, @NotNull ComparingResult expectedResult) {
        ComparingResult result = ComparingResult.fromInt(comparator.compare(ver1, ver2));
        assertSame(expectedResult, result, "'%s' and '%s'".formatted(ver1, ver2));
    }


    private enum ComparingResult {
        UPPER, EQUALS, LOWER;

        public static @NotNull ComparingResult fromInt(int i) {
            if (i < 0) {
                return LOWER;
            } else if (i > 0) {
                return UPPER;
            } else {
                return EQUALS;
            }
        }
    }
}
