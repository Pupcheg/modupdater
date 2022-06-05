package me.supcheg.modupdater.common.util;

import me.supcheg.modupdater.common.mod.ModInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

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
        return new DownloadResult(null, Util.EMPTY_STRING_ARRAY, e);
    }

    public static @NotNull DownloadResult createError(@NotNull String message, @NotNull Exception e) {
        return new DownloadResult(null, new String[]{message}, e);
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

    @Override
    public String toString() {
        return new StringJoiner(", ", "DownloadResult{", "}")
                .add("result=" + result)
                .add("message=" + Arrays.toString(message))
                .add("exception=" + exception)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadResult that = (DownloadResult) o;
        return Objects.equals(result, that.result) && Arrays.equals(message, that.message) && Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(result, exception);
        hash = 31 * hash + Arrays.hashCode(message);
        return hash;
    }
}
