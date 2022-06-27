package me.supcheg.modupdater.common.downloader;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class SimpleModDownloader extends ModDownloader {

    private final BiFunction<IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor, DownloadConfig, DownloadResult> downloadFunction;
    private final Predicate<String> canDownloadPredicate;

    public SimpleModDownloader(@NotNull Updater updater,
                               @NotNull String name,
                               @NotNull BiFunction<IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor, DownloadConfig, DownloadResult> function,
                               @NotNull Predicate<String> predicate) {
        super(name, updater);
        this.downloadFunction = function;
        this.canDownloadPredicate = predicate;
    }

    @NotNull
    @Override
    protected DownloadResult downloadLatest(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                            @NotNull DownloadConfig downloadConfig) {
        return downloadFunction.apply(accessor, downloadConfig);
    }

    @Override
    public boolean canDownload(@NotNull String url) {
        return canDownloadPredicate.test(url);
    }
}
