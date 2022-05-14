package me.supcheg.modupdater.base;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.therandomlabs.curseapi.util.OkHttpUtils;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    private static final Gson prettyJson = new GsonBuilder().setPrettyPrinting().create();
    private static final Collector<CharSequence, ?, String> joining = Collectors.joining(", ");
    private static final Field elementsField;

    static {
        try {
            elementsField = JsonArray.class.getDeclaredField("elements");
            elementsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<String> asList(@NotNull JsonArray array) {
        List<JsonElement> elements;
        try {
            //noinspection unchecked
            elements = (List<JsonElement>) elementsField.get(array);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Lists.transform(elements, JsonElement::getAsString);
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
}
