package me.supcheg.modupdater.common.util;

import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public final class JarFileExplorer {

    private JarFileExplorer() {}

    public static @NotNull JarFileDescription getDescription(@NotNull Path path) throws IOException {
        try (JarFile jarFile = new JarFile(path.toFile())) {
            return getDescription(jarFile);
        }
    }

    public static @NotNull JarFileDescription getDescription(@NotNull JarFile jarFile) throws IOException {
        JarEntry fabric = jarFile.getJarEntry("fabric.mod.json");
        if (fabric != null)
            return JarFileDescription.createFabric(JsonParser.parseString(read(jarFile, fabric)).getAsJsonObject());
        JarEntry quilt = jarFile.getJarEntry("quilt.mod.json");
        if (quilt != null) {
            return JarFileDescription.createQuilt(JsonParser.parseString(read(jarFile, quilt)).getAsJsonObject());
        }

        JarEntry forge = jarFile.getJarEntry("META-INF/mods.toml");
        if (forge != null) {
            JarEntry manifest = jarFile.getJarEntry("META-INF/MANIFEST.MF");
            return JarFileDescription.createForge(
                    read(jarFile, manifest).split("\n"),
                    read(jarFile, forge).split("\n")
            );
        }

        throw new IllegalArgumentException(jarFile.getName() + " isn't a fabric/forge/quilt mod");
    }

    private static @NotNull String read(@NotNull JarFile jarFile, @NotNull JarEntry entry) throws IOException {
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)))) {
            return buf.lines().collect(Collectors.joining("\n"));
        }
    }
}
