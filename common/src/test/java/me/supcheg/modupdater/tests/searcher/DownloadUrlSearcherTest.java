package me.supcheg.modupdater.tests.searcher;

import com.google.common.collect.ImmutableMap;
import me.supcheg.modupdater.common.searcher.DownloadUrlSearcher;
import me.supcheg.modupdater.tests.DummyMod;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DownloadUrlSearcherTest {

    public static void test(@NotNull DownloadUrlSearcher searcher) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        Map<String, String> expectedValues = builder
                .put("AppleSkin", "https://modrinth.com/mod/appleskin")
                .put("Fabric", "https://modrinth.com/mod/fabric-api")
                .put("Borderless mining", "https://modrinth.com/mod/borderless-mining")
                .put("Architectury", "https://modrinth.com/mod/architectury-api")
                .put("bobby", "https://modrinth.com/mod/bobby")
                .put("indium", "https://modrinth.com/mod/indium")
                .build();

        for (var entry : expectedValues.entrySet()) {
            String name = entry.getKey();
            String url = entry.getValue();

            DummyMod dummyMod = new DummyMod(searcher.getUpdater(), name, name.toLowerCase().replace(" ", "_"));
            assertEquals(searcher.find(dummyMod), url);
        }
    }
}
