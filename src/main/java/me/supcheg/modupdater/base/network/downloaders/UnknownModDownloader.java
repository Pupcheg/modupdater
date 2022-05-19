package me.supcheg.modupdater.base.network.downloaders;

import me.supcheg.modupdater.base.Updater;
import me.supcheg.modupdater.base.mod.ModInstance;
import me.supcheg.modupdater.base.network.DownloadConfig;
import me.supcheg.modupdater.base.network.DownloadResult;
import me.supcheg.modupdater.base.network.ModDownloader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnknownModDownloader extends ModDownloader {

    public UnknownModDownloader(@NotNull Updater updater) {
        super("Unknown", "Just copies the last file.", updater);
    }

    @Override
    public boolean canDownload(@NotNull String url) {
        return true;
    }

    @NotNull
    @Override
    public DownloadResult downloadLatest(@NotNull DownloadConfig downloadConfig) {
        try {
            // Simply copy the latest instance
            ModInstance latest = downloadConfig.getMod().getLatestInstance();
            Files.copy(latest.getPath(), Path.of(downloadConfig.getDownloadFolder().toString(), latest.getPath().getFileName().toString()));
            return DownloadResult.createSuccess(downloadConfig.getMod().getLatestInstance());
        } catch (IOException e) {
            return DownloadResult.createError(e);
        }
    }
}
