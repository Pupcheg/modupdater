package me.supcheg.modupdater.tests.comparator;

import com.google.gson.JsonObject;
import me.supcheg.modupdater.common.comparator.VersionComparator;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static me.supcheg.modupdater.tests.comparator.ComparingResult.*;
import static org.junit.jupiter.api.Assertions.assertSame;

public class VersionComparatorTest {

    public static void test(@NotNull VersionComparator comparator) {

        Map<Pair<String, String>, ComparingResult> map = ComparingMapBuilder.create()
                .put("1", "2", LOWER)
                .put("2", "2", EQUALS)
                .put("10", "1", UPPER)
                .put("2.3", "1.0.1", UPPER)
                .put("3.10.1", "4.0.0", LOWER)
                .put("3.0.0-alpha", "3.0.0-beta", LOWER)
                .put("3.0.0", "3.0.0-beta", UPPER)
                .put("3.0.0-beta", "3.0.0-release", LOWER)
                .put("3.0.0", "3.0", EQUALS)
                .put("[1.18.2] 1.0", "[1.18.2] 2.0", LOWER)
                .put("[1.18.2] 3.1.0", "2.0", UPPER)
                .put("[1.19] 2.0.0", "2", EQUALS)
                .put("[ALPHA] 4.0.0", "4.0.0", LOWER)
                .put("1.19-2.9.9", "2.9.8", UPPER)
                .artificiallyIncrease()
                .build();

        test(comparator, map, UnaryOperator.identity(), "Standard");
        test(comparator.reversed(), map, ComparingResult::reverse, "Reversed");

        Map<Pair<JsonObject, JsonObject>, ComparingResult> transformedMap = new HashMap<>();
        map.forEach((pair, result) -> transformedMap.put(asJsonObjectPair(pair), result));

        test(comparator.transform(o -> o.get("version").getAsString()), transformedMap, UnaryOperator.identity(), "Standard Transforming");
        test(comparator.reversed().transform(o -> o.get("version").getAsString()), transformedMap, ComparingResult::reverse, "Reversed Transforming");
    }


    private static @NotNull Pair<JsonObject, JsonObject> asJsonObjectPair(@NotNull Pair<String, String> pair) {
        return Pair.of(asJsonObject(pair.getLeft()), asJsonObject(pair.getRight()));
    }

    private static @NotNull JsonObject asJsonObject(@NotNull String value) {
        JsonObject j = new JsonObject();
        j.addProperty("version", value);
        return j;
    }

    private static <T> void test(@NotNull Comparator<T> comparator, @NotNull Map<Pair<T, T>, ComparingResult> map,
                                 @NotNull UnaryOperator<ComparingResult> unaryOperator, @NotNull String comparatorName) {
        for (var entry : map.entrySet()) {
            Pair<T, T> pair = entry.getKey();

            T version1 = pair.getLeft();
            T version2 = pair.getRight();

            ComparingResult expectedResult = unaryOperator.apply(entry.getValue());

            ComparingResult result;
            try {
                result = ComparingResult.fromInt(comparator.compare(version1, version2));
            } catch (Exception ex) {
                throw new RuntimeException("'%s' and '%s'".formatted(version1, version2), ex);
            }

            assertSame(expectedResult, result, "%s: '%s' and '%s'".formatted(comparatorName, version1, version2));
        }
    }
}
