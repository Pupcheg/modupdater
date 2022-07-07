package me.supcheg.modupdater.tests;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.mod.ModType;
import me.supcheg.modupdater.tests.comparator.VersionComparatorTest;
import me.supcheg.modupdater.tests.searcher.DownloadUrlSearcherTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class UpdaterTest {

    private static final RuntimeConfig config;
    public static final Updater UPDATER;

    static {
        UPDATER = Updater.builder()
                .defaultHttpClient()
                .defaultDownloaders()
                .defaultExecutor()
                .defaultUrlSearchers()
                .defaultVersionComparator()
                .config(u -> {
                    try {
                        return new RuntimeConfig(u, "1.19", ModType.FABRIC);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();
        config = (RuntimeConfig) UPDATER.getConfig();
    }

    @Test
    public void run() throws IOException {
        VersionComparatorTest.test(UPDATER.getVersionComparator());
        DownloadUrlSearcherTest.test(UPDATER.getUrlSearcher());
        config.deleteDirectories();
    }
}
