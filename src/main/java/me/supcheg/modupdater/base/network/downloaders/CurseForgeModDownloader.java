package me.supcheg.modupdater.base.network.downloaders;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFiles;
import com.therandomlabs.curseapi.project.CurseProject;
import me.supcheg.modupdater.base.mod.Mod;
import me.supcheg.modupdater.base.mod.ModType;
import me.supcheg.modupdater.base.mod.SmallModInfo;
import me.supcheg.modupdater.base.network.DownloadConfig;
import me.supcheg.modupdater.base.network.DownloadResult;
import me.supcheg.modupdater.base.network.ModDownloader;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class CurseForgeModDownloader extends ModDownloader {

    public CurseForgeModDownloader() {
        super("CurseForge", "Can download mods from https://www.curseforge.com/minecraft. Specific download data: numeric project id.");
    }

    @NotNull
    @Override
    public DownloadResult downloadLatest(@NotNull DownloadConfig downloadConfig) {
        Mod mod = downloadConfig.getMod();
        try {
            CurseProject project = null;

            // Get from id if mod have a specified download data
            String specific = mod.getSpecificDownloadData();
            if (specific != null) {
                try {
                    project = CurseAPI.project(Integer.parseInt(mod.getSpecificDownloadData())).orElseThrow();
                } catch (Exception ignored) {
                }
            }

            // Project not found or specified download data is null
            if (project == null) {
                String projectPath = HttpUrl.get(mod.getUrl()).encodedPath().replace("projects", "minecraft/mc-mods");
                project = CurseAPI.project(projectPath).orElse(null);
            }

            // Can't find project anyway
            if (project == null) {
                return DownloadResult.createError(
                        "No project match for " + mod.getName(),
                        "Possible solution: You can change download url or set project id (specific download data)."
                );
            }

            CurseFiles<CurseFile> files = project.files();
            if (files.isEmpty()) {
                return DownloadResult.createError("The project is empty.");
            }

            for (CurseFile file : files) {
                Set<String> gameVersions = file.gameVersionStrings();

                SmallModInfo info = new SmallModInfo(gameVersions, ModType.fromStringCollection(gameVersions));
                if (!downloadConfig.isCorrect(info)) continue;

                Path download = downloadConfig.getDownloadFolder().resolve(file.nameOnDisk());
                try (InputStream in = file.downloadURL().url().openStream()) {
                    Files.copy(in, download, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    return DownloadResult.createError(e);
                }
                return DownloadResult.createSuccess(mod.createInstance(download));
            }

            return DownloadResult.createError("A file suitable for the specified parameters was not found.");
        } catch (Exception e) {
            return DownloadResult.createError(e);
        }
    }
}
