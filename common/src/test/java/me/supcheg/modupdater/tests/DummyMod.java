package me.supcheg.modupdater.tests;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.mod.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DummyMod extends Mod {

    private String url;

    public DummyMod(@NotNull Updater updater, @NotNull String name, @NotNull String id) {
        super(updater, name, id);
    }

    @Nullable
    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(@NotNull String url) {
        this.url = url;
    }

    private static final class Builder {

    }
}
