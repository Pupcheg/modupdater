package me.supcheg.modupdater.base.mod;

import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class JarFileExplorer {

    public static @NotNull ModDescription getDescription(@NotNull Path path) throws IOException {
        try (JarFile jarFile = new JarFile(path.toFile())) {
            return getDescription(jarFile);
        }
    }

    public static @NotNull ModDescription getDescription(@NotNull JarFile jarFile) throws IOException {
        JarEntry fabric = jarFile.getJarEntry("fabric.mod.json");
        if (fabric != null)
            return ModDescription.createFabric(JsonParser.parseString(read(jarFile, fabric)).getAsJsonObject());
        JarEntry quilt = jarFile.getJarEntry("quilt.mod.json");
        if (quilt != null) {
            return ModDescription.createQuilt(JsonParser.parseString(read(jarFile, quilt)).getAsJsonObject());
        }

        JarEntry forge = jarFile.getJarEntry("META-INF/mods.toml");
        if (forge != null) {
            JarEntry manifest = jarFile.getJarEntry("META-INF/MANIFEST.MF");
            return ModDescription.createForge(
                    read(jarFile, manifest).split("\n"),
                    read(jarFile, forge).split("\n")
            );
        }

        throw new IllegalArgumentException(jarFile.getName() + " isn't a fabric/forge mod");
    }

    private static @NotNull String read(@NotNull JarFile jarFile, @NotNull JarEntry entry) throws IOException {
        try (InputStream in = jarFile.getInputStream(entry)) {
            try (InputStreamReader reader = new InputStreamReader(in)) {
                try (BufferedReader buf = new BufferedReader(reader)) {
                    return buf.lines().collect(Collectors.joining("\n"));
                }
            }
        }
    }
}
