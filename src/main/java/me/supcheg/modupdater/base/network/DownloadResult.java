package me.supcheg.modupdater.base.network;

import me.supcheg.modupdater.base.mod.ModInstance;
import org.jetbrains.annotations.NotNull;

public class DownloadResult {

    private final ModInstance result;
    private final String[] message;
    private final Exception exception;

    private DownloadResult(ModInstance result, String[] message, Exception exception) {
        this.result = result;
        this.message = message;
        this.exception = exception;
    }

    public static @NotNull DownloadResult createError(@NotNull Exception e) {
        return new DownloadResult(null, new String[]{e.getClass().getName(), e.getMessage()}, e);
    }

    public static @NotNull DownloadResult createError(@NotNull String message, @NotNull Exception e) {
        return new DownloadResult(null, new String[]{message, e.getClass().getName(), e.getMessage()}, e);
    }

    public static @NotNull DownloadResult createError(@NotNull String... message) {
        return new DownloadResult(null, message, null);
    }

    public static @NotNull DownloadResult createSuccess(@NotNull ModInstance result) {
        return new DownloadResult(result, null, null);
    }

    public ModInstance getResult() {
        return result;
    }

    public String[] getMessage() {
        return message;
    }

    public Exception getException() {
        return exception;
    }

    public boolean isSuccess() {
        return result != null;
    }
}
