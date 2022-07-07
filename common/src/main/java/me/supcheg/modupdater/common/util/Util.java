package me.supcheg.modupdater.common.util;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.therandomlabs.curseapi.util.OkHttpUtils;
import me.supcheg.modupdater.common.Updater;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
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

    public static @NotNull JsonElement readJson(@NotNull OkHttpClient client, @NotNull String url) throws IOException {
        return readJson(client, HttpUrl.get(url));
    }

    public static @NotNull JsonElement readJson(@NotNull OkHttpClient client, @NotNull HttpUrl url) throws IOException {
        try {
            Request req = new Request.Builder().url(url).build();
            try (Response response = client.newCall(req).execute()) {
                ResponseBody body = response.body();
                if (body == null) {
                    throw new NullPointerException("ResponseBody is null");
                }
                return JsonParser.parseString(body.string());
            }
        } catch (JsonSyntaxException ex) {
            try (InputStream in = new URL(url.toString().replace("%22", "")).openStream()) {
                return JsonParser.parseString(new String(in.readAllBytes()));
            }
        }

    }

    public static void copy(@NotNull HttpUrl from, @NotNull Path to, @NotNull OkHttpClient client) throws IOException {
        Request req = new Request.Builder().url(from).build();
        try (Response response = client.newCall(req).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new NullPointerException("ResponseBody is null");
            }
            Files.copy(body.byteStream(), to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static @NotNull String join(@NotNull Stream<String> strings) {
        return strings.distinct().collect(joining);
    }

    public static void validateSameUpdater(@NotNull UpdaterHolder firstHolder, @NotNull UpdaterHolder secondHolder) {
        Updater first = firstHolder.getUpdater();
        Updater second = secondHolder.getUpdater();

        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");

        if (first != second) {
            String message = "%s and %s has different %s's instances"
                    .formatted(firstHolder, secondHolder, Updater.class.getSimpleName());
            throw new IllegalStateException(message);
        }
    }
}
