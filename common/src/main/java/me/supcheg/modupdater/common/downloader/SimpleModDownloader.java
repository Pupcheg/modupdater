package me.supcheg.modupdater.common.downloader;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SimpleModDownloader extends ModDownloader {

    private final DownloadFunction downloadFunction;
    private final Predicate<String> canDownloadPredicate;

    public SimpleModDownloader(@NotNull Updater updater,
                               @NotNull String name,
                               @NotNull DownloadFunction function,
                               @NotNull Predicate<String> predicate) {
        super(name, updater);
        this.downloadFunction = function;
        this.canDownloadPredicate = predicate;
    }

    @NotNull
    @Override
    protected DownloadResult downloadLatest(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                            @NotNull DownloadConfig downloadConfig) {
        try {
            return downloadFunction.download(accessor, downloadConfig, updater);
        } catch (Exception e) {
            return DownloadResult.createError(e);
        }
    }

    @Override
    public boolean canDownload(@NotNull String url) {
        return canDownloadPredicate.test(url);
    }

    public interface DownloadFunction {
        @NotNull
        DownloadResult download(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                @NotNull DownloadConfig downloadConfig,
                                @NotNull Updater updater);
    }
}
