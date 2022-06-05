package me.supcheg.modupdater.common.downloader;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFiles;
import com.therandomlabs.curseapi.project.CurseProject;
import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModType;
import me.supcheg.modupdater.common.mod.SupportInfo;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;

public class CurseForgeModDownloader extends ModDownloader {

    public CurseForgeModDownloader(@NotNull Updater updater) {
        super("CurseForge", "Can download mods from https://www.curseforge.com/minecraft. Specific download data: numeric project id.", updater);
    }

    @NotNull
    @Override
    protected DownloadResult downloadLatest(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                         @NotNull DownloadConfig downloadConfig) {

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
                String projectPath = HttpUrl.get(Objects.requireNonNull(mod.getUrl())).encodedPath().replace("projects", "minecraft/mc-mods");
                project = CurseAPI.project(projectPath).orElse(null);
            }

            // Can't find project anyway
            if (project == null) {
                return DownloadResult.createError(
                        "No project match for " + mod.getId(),
                        "Possible solution: You can change download url or set project id (specific download data)."
                );
            }

            CurseFiles<CurseFile> files = project.files();
            if (files.isEmpty()) {
                return DownloadResult.createError("The project is empty.");
            }

            for (CurseFile file : files) {
                Set<String> gameVersions = file.gameVersionStrings();

                SupportInfo info = new SupportInfo(gameVersions, ModType.fromStringCollection(gameVersions));
                if (!downloadConfig.isCorrect(info)) continue;
                accessor.set("Downloading file");

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
