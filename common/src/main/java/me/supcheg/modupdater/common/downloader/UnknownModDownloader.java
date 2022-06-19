package me.supcheg.modupdater.common.downloader;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModInstance;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnknownModDownloader extends ModDownloader {

    public UnknownModDownloader(@NotNull Updater updater) {
        super("Unknown", updater);
    }

    @Override
    public boolean canDownload(@NotNull String url) {
        return true;
    }

    @NotNull
    @Override
    protected DownloadResult downloadLatest(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                            @NotNull DownloadConfig downloadConfig) {
        try {
            // Simply copy the latest instance
            Mod mod = downloadConfig.getMod();
            ModInstance latest = mod.getLatestInstance();

            Path downloadPath = downloadConfig.getDownloadFolder().resolve(latest.getPath().getFileName());
            Files.copy(latest.getPath(), downloadPath);

            return DownloadResult.createSuccess(mod.createInstance(downloadPath));
        } catch (IOException e) {
            return DownloadResult.createError(e);
        }
    }
}
