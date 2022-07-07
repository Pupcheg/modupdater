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
import me.supcheg.modupdater.common.util.Util;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public class CurseForgeModDownloader extends ModDownloader {

    public CurseForgeModDownloader(@NotNull Updater updater) {
        super("CurseForge", updater);
    }

    @NotNull
    @Override
    protected DownloadResult downloadLatest(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                            @NotNull DownloadConfig downloadConfig) {

        Mod mod = downloadConfig.getMod();

        try {

            String projectPath = HttpUrl.get(Objects.requireNonNull(mod.getUrl())).encodedPath().replace("projects", "minecraft/mc-mods");
            CurseProject project = CurseAPI.project(projectPath).orElse(null);

            if (project == null) {
                return DownloadResult.createError("No project match for %s.".formatted(mod.getId()));
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

                Util.copy(file.downloadURL(), download, updater.getHttpClient());
                return DownloadResult.createSuccess(mod.createInstance(download));
            }

            return DownloadResult.createError("A file suitable for the specified parameters was not found.");
        } catch (Exception e) {
            return DownloadResult.createError(e);
        }
    }
}
