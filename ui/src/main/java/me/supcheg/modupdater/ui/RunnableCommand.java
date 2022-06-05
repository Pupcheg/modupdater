package me.supcheg.modupdater.ui;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class RunnableCommand implements Comparable<RunnableCommand> {

    public static @NotNull Builder name(@NotNull String name) {
        return new Builder().name(name);
    }

    private final String fullName;
    private final String usage;
    private final String description;
    private final String[] aliases;
    private final Consumer<String[]> consumer;

    public RunnableCommand(@NotNull String fullName, @NotNull String usage, @NotNull String description,
                           @NotNull String[] aliases, @NotNull Runnable runnable) {
        this(fullName, usage, description, aliases, a -> runnable.run());
    }

    public RunnableCommand(@NotNull String fullName, @NotNull String usage, @NotNull String description,
                           @NotNull String[] aliases, @NotNull Consumer<String[]> consumer) {
        this.fullName = fullName;
        this.usage = usage;
        this.description = description;
        this.aliases = aliases;
        this.consumer = consumer;
    }

    private RunnableCommand(@NotNull Builder builder) {
        this.fullName = builder.fullName;
        this.usage = builder.usage;
        this.description = builder.description;
        this.aliases = builder.aliases;
        this.consumer = builder.consumer;
    }

    public @NotNull String getFullName() {
        return fullName;
    }

    public @NotNull String getUsage() {
        return usage;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public @NotNull String[] getAliases() {
        return aliases;
    }

    public void run(@NotNull String[] args) {
        consumer.accept(args);
    }

    @Override
    public int compareTo(@NotNull RunnableCommand o) {
        return fullName.compareTo(o.fullName);
    }

    public static class Builder {
        private String fullName;
        private String usage;
        private String description;
        private String[] aliases;
        private Consumer<String[]> consumer;

        private Builder() {}

        public @NotNull Builder name(@NotNull String fullName) {
            this.fullName = fullName;
            return this;
        }

        public @NotNull Builder usage(@NotNull String usage) {
            this.usage = usage;
            return this;
        }

        public @NotNull Builder description(@NotNull String description) {
            this.description = description;
            return this;
        }

        public @NotNull Builder aliases(@NotNull String... aliases) {
            this.aliases = aliases;
            return this;
        }

        public @NotNull Builder function(@NotNull Runnable runnable) {
            return function(ignored -> runnable.run());
        }

        public @NotNull Builder function(@NotNull Consumer<String[]> consumer) {
            this.consumer = consumer;
            return this;
        }

        public @NotNull RunnableCommand build() {
            requireNonNull(fullName);
            if (usage == null) {
                usage = fullName.toLowerCase();
            }
            requireNonNull(description);
            requireNonNull(aliases);
            if (aliases.length == 0) throw new IllegalStateException();
            requireNonNull(consumer);

            return new RunnableCommand(this);
        }
    }
}
