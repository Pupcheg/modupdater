package me.supcheg.modupdater.common.util;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.therandomlabs.curseapi.util.OkHttpUtils;
import me.supcheg.modupdater.common.Updater;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Util {

    private Util() {}

    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Field elementsField;
    private static final Gson prettyJson = new GsonBuilder().setPrettyPrinting().create();
    private static final Collector<CharSequence, ?, String> joining = Collectors.joining(", ");

    static {
        try {
            elementsField = JsonArray.class.getDeclaredField("elements");
            elementsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        Duration defaultTimeout = Duration.ofMillis(5000);

        var newClient = new OkHttpClient.Builder()
                .connectTimeout(defaultTimeout)
                .readTimeout(defaultTimeout)
                .writeTimeout(defaultTimeout)
                .retryOnConnectionFailure(true)
                .build();

        OkHttpUtils.setClient(newClient);
    }

    @NotNull
    public static <T> Stream<T> stream(@NotNull Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static @NotNull List<JsonElement> asList(@NotNull JsonArray array) {
        try {
            //noinspection unchecked
            return (List<JsonElement>) elementsField.get(array);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<String> asStringList(@NotNull JsonArray array) {
        return Lists.transform(asList(array), JsonElement::getAsString);
    }

    public static @NotNull String toPrettyString(JsonElement element) {
        return prettyJson.toJson(element);
    }

    public static @NotNull String read(@NotNull String url) {
        Request request = new Request.Builder().url(url).build();

        try (Response response = OkHttpUtils.getClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RuntimeException();
            }

            return body.string();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read from URL: " + url, ex);
        }
    }

    public static @NotNull String join(@NotNull Stream<String> strings) {
        return strings.distinct().collect(joining);
    }

    public static void validateSameUpdater(@NotNull UpdaterHolder firstHolder, @NotNull UpdaterHolder secondHolder) {
        Updater first = firstHolder.getUpdater();
        Updater second = secondHolder.getUpdater();

        if (first != null && second != null && first != second) {
            String message = "%s and %s has different %s's instances"
                    .formatted(firstHolder, secondHolder, Updater.class.getSimpleName());
            throw new IllegalStateException(message);
        }
    }
}
