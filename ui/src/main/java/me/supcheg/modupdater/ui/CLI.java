package me.supcheg.modupdater.ui;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.concurrent.ChangeConsumer;
import me.supcheg.modupdater.common.config.JsonConfig;
import me.supcheg.modupdater.common.downloader.ModDownloader;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModInstance;
import me.supcheg.modupdater.common.mod.ModType;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import me.supcheg.modupdater.common.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class CLI {

    public static void main(String[] args) {
        try {
            new CLI().start(args);
        } catch (IOException e) {
            System.out.println("Error while enabling:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static final String COMMAND_INLINE = "> ";
    private static final String TAB = "  ";

    private boolean firstLaunch;
    private final Scanner scanner;
    private final JsonConfig config;
    private final Updater updater;
    private final Map<String, RunnableCommand> commands;
    private final RunnableCommand defaultCommand;

    public CLI() throws IOException {
        scanner = new Scanner(System.in);

        Path configPath = Path.of(System.getProperty("user.dir") + "/updater.cfg.json");
        if (Files.notExists(configPath)) {
            Files.createFile(configPath);
            Files.writeString(configPath, "{}");
            firstLaunch = true;
        }

        updater = Updater.builder()
                .defaultHttpClient()
                .defaultDownloaders()
                .defaultExecutor()
                .defaultUrlSearchers()
                .defaultVersionComparator()
                .config(u -> {
                    try {
                        return new JsonConfig(u, configPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();
        config = (JsonConfig) updater.getConfig();
        if (!firstLaunch) {
            updater.reloadDefaultModsFolder();
        }
        commands = new TreeMap<>();

        defaultCommand = new RunnableCommand("", "", "", Util.EMPTY_STRING_ARRAY, () -> println("Unknown command. Type 'help' to get help."));

        addCommands(
                RunnableCommand.name("Setup")
                        .description("Run setup.")
                        .aliases("setup")
                        .function(this::setup),

                RunnableCommand.name("List")
                        .description("List all loaded mods.")
                        .aliases("list", "l")
                        .function(this::listMods),

                RunnableCommand.name("Download")
                        .usage("download ?[mod(-s)/downloader]")
                        .description("Download all loaded mods. You can specify the name of one mod or loader.")
                        .aliases("download")
                        .function(args -> genericFunctionWithModCollection(args, this::download)),

                RunnableCommand.name("Set url")
                        .usage("set-url [mod] [url]")
                        .description("Set a download url for the specified mod.")
                        .aliases("set-url", "su")
                        .function(args -> {
                            if (checkArgsLength("set-url", args.length, 2)) return;

                            Mod mod = findMod(args[0]);
                            if (mod == null) return;

                            if (isInvalidUrl(args[1])) {
                                printf("Invalid url: %s", args[1]);
                                return;
                            }

                            mod.setUrl(args[1]);
                            mod.setDownloader(updater.findModDownloaderForUrl(args[1]));

                            printf("A download url is set for the '%s' mod: %s.", args[0], args[1]);
                        }),

                RunnableCommand.name("Info")
                        .usage("info [mod]")
                        .description("Get information about the mod.")
                        .aliases("info", "i")
                        .function(args -> {
                            if (checkArgsLength("info", args.length, 1)) return;

                            Mod mod = findMod(args[0]);
                            if (mod == null) return;

                            println(
                                    mod.getName() + ':',
                                    TAB + "Id: " + mod.getId(),
                                    TAB + "Versions: " + Util.join(mod.getInstances().stream().map(ModInstance::getVersion)),
                                    TAB + "Types: " + Util.join(mod.getInstances().stream().map(ModInstance::getType).map(ModType::getName)),
                                    TAB + "Url: " + mod.getUrl(),
                                    TAB + "Downloader: " + mod.getDownloader().getName(),
                                    TAB + "Files: " + Util.join(mod.getInstances().stream().map(ModInstance::getPath).map(Path::toString))
                            );
                        }),

                RunnableCommand.name("Config")
                        .description("Show the config used.")
                        .aliases("config")
                        .function(() -> println(Util.toPrettyString(config.getJsonObject()).split("\n"))),

                RunnableCommand.name("Help")
                        .description("Get help.")
                        .aliases("help")
                        .function(() -> {
                            // ModDownloaders info
                            var downloaders = this.updater.getModDownloaders()
                                    .stream()
                                    .sorted(Comparator.comparing(ModDownloader::getName))
                                    .distinct()
                                    .toList();

                            println("Downloaders:");
                            for (ModDownloader modDownloader : downloaders) {
                                println(TAB + modDownloader.getName());
                            }

                            println();
                            // Commands info
                            var commands = this.commands.values().stream()
                                    .distinct()
                                    .sorted()
                                    .toList();

                            println("Commands:");
                            for (RunnableCommand command : commands) {
                                println(
                                        TAB + command.getFullName(),
                                        TAB + TAB + "Description: " + command.getDescription(),
                                        TAB + TAB + "Usage: " + command.getUsage(),
                                        TAB + TAB + "Aliases: " + Util.join(Arrays.stream(command.getAliases()))
                                );
                            }
                        }),

                RunnableCommand.name("Load")
                        .description("Load mods from the default mods folder.")
                        .aliases("load")
                        .function(() -> {
                            updater.reloadDefaultModsFolder();
                            listMods();
                        }),

                RunnableCommand.name("Exit")
                        .description("Exit the command line interface.")
                        .aliases("exit")
                        .function(() -> {
                            updater.close();
                            System.exit(0);
                        }),

                RunnableCommand.name("Install")
                        .usage("install ?[mod(-s)/downloader]")
                        .description("Downloads and installs the specified mods")
                        .aliases("install")
                        .function(args -> genericFunctionWithModCollection(args, this::install))
        );
    }


    // Commands registration
    public void addCommand(@NotNull RunnableCommand command) {
        for (String alias : command.getAliases()) {
            commands.put(alias, command);
        }
    }

    public void addCommands(@NotNull RunnableCommand.Builder first, @NotNull RunnableCommand.Builder... other) {
        addCommand(first.build());
        for (RunnableCommand.Builder builder : other) {
            addCommand(builder.build());
        }
    }


    // Running commands
    public void start(@NotNull String[] initialArgs) {
        if (initialArgs.length != 0) {
            try {
                // User have specified command in args, run it and exit
                execute(initialArgs[0], Arrays.copyOfRange(initialArgs, 1, initialArgs.length));
                System.exit(0);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (firstLaunch) {
            setup();
            println();
            println("If you have entered incorrect data or want to change it, use the 'setup' command.");
        }

        println("Type 'help' to get help.");
        //noinspection InfiniteLoopStatement
        while (true) {
            String[] ss = input().split(" ");
            String command = ss.length > 0 ? ss[0] : "";
            String[] args;
            if (ss.length == 1) {
                args = Util.EMPTY_STRING_ARRAY;
            } else {
                args = Arrays.copyOfRange(ss, 1, ss.length);
            }
            try {
                execute(command, args);
                println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void execute(@NotNull String command, String[] args) {
        commands.getOrDefault(command.toLowerCase(), defaultCommand).run(args);
    }


    // Commands
    private void genericFunctionWithModCollection(@NotNull String[] args, @NotNull BiConsumer<DownloadConfig.Builder, Collection<Mod>> consumer) {
        DownloadConfig.Builder downloadConfig = DownloadConfig.builder().defaultsFrom(config);

        // If specified mod(-s)/downloader
        if (args.length > 0) {
            Set<String> notFound = new HashSet<>();
            Set<Mod> mods = new HashSet<>();

            for (String arg : args) {
                Mod mod = updater.findMod(arg);
                if (mod == null) {
                    notFound.add(arg);
                } else {
                    mods.add(mod);
                }
            }

            if (!mods.isEmpty()) {
                if (!notFound.isEmpty()) {
                    printf("Unknown mods: %s", String.valueOf(notFound));
                }
                consumer.accept(downloadConfig, mods);
                return;
            }


            ModDownloader downloader = updater.findModDownloaderByName(args[0]);
            if (downloader != null) {
                // Accept all mods with specified ModDownloader
                var correctMods = updater.getMods().stream()
                        .filter(m -> downloader.equals(m.getDownloader()))
                        .collect(Collectors.toSet());
                consumer.accept(downloadConfig, correctMods);
                return;
            }

            // Can't find any Mod or ModDownloader with this name
            printf("The mod or mod downloader with the name '%s' was not found.", args[0]);
            return;
        }

        // Accept all
        consumer.accept(downloadConfig, updater.getMods());
    }

    private void setup() {
        println("Specify the download folder:");
        Path downloadFolder;
        do {
            downloadFolder = buildPath(input());
        } while (downloadFolder == null);
        config.set("download_folder", downloadFolder.toString());

        println("Specify the mods folder:");
        Path modsFolder;
        do {
            modsFolder = buildPath(input());
        } while (modsFolder == null);
        config.set("mods_folder", modsFolder.toString());

        println("Specify the mods type (fabric/forge):");
        ModType modsType = null;
        while (modsType == null) {
            String input = input();
            try {
                modsType = ModType.valueOf(input.toUpperCase());
            } catch (Exception ignored) {
                printf("Invalid mod type: %s", input);
            }
        }
        config.set("mods_type", modsType.getName());

        println("Specify the minecraft version:");
        String minecraftVersion = null;
        do {
            String input = input().trim();
            if (input.isEmpty()) {
                continue;
            }
            String[] split = input.split("\\.");
            if (split.length == 1) {
                println("Incorrect version format");
                continue;
            }
            minecraftVersion = split[0] + '.' + split[1];

        } while (minecraftVersion == null);
        config.set("minecraft_version", minecraftVersion);

        println("Specify the GitHub token (you can get it at https://github.com/settings/tokens):");
        String githubToken = input().trim();
        if (!githubToken.isEmpty()) {
            config.set("github_token", githubToken);
        }
        updater.reloadDefaultModsFolder();
    }

    private void listMods() {
        Collection<Mod> mods = updater.getMods();
        printf("Mods (%d):", mods.size());
        if (mods.isEmpty()) {
            println("[empty]");
        } else {
            for (ModDownloader downloader : updater.getModDownloaders()) {

                Set<Mod> validMods = mods.stream()
                        .filter(m -> m.getDownloader().equals(downloader))
                        .collect(Collectors.toSet());

                printf(TAB + "%s (%d):", downloader.getName(), validMods.size());

                for (Mod mod : validMods) {
                    String versions = Util.join(mod.getInstances().stream().map(ModInstance::getVersion));
                    printf(TAB + TAB + "%s (%s)", mod.getName(), versions);
                }
            }
        }
    }

    private void download(@NotNull DownloadConfig.Builder downloadConfig, @NotNull Collection<Mod> mods) {
        Set<Mod> updated = new HashSet<>();
        Set<Mod> notUpdated = new HashSet<>();

        for (Mod mod : mods) {
            printf("Downloading %s", mod.getName());

            DownloadResult result = updater.downloadLatest(downloadConfig.buildCopyWithMod(mod), ChangeConsumer.onlyNewState(CLI::println), null)
                    .get();
            if (result.isSuccess()) {
                printf("Successful, filename: %s.", result.getResult().getPath().getFileName().toString());
                updated.add(mod);
            } else {
                printf("Error while downloading %s:", mod.getName());
                printError(TAB, result);
                notUpdated.add(mod);
            }
        }

        printf("Downloaded (%d):", updated.size());
        for (Mod mod : updated) {
            println(TAB + mod.getName());
        }

        printf("Not downloaded (%d):", notUpdated.size());
        for (Mod mod : notUpdated) {
            printf("%s%s (%s)", TAB, mod.getName(), mod.getDownloader().getName());
        }
    }

    private void install(@NotNull DownloadConfig.Builder downloadConfig, @NotNull Collection<Mod> mods) {
        Set<Mod> installed = new HashSet<>();
        Set<Mod> notInstalled = new HashSet<>();

        for (Mod mod : mods) {
            printf("Downloading %s", mod.getName());
            DownloadResult result = updater.downloadLatest(downloadConfig.buildCopyWithMod(mod), ChangeConsumer.onlyNewState(CLI::println), null)
                    .get();
            if (result.isSuccess()) {
                printf("Successful, filename: %s.", result.getResult().getPath().getFileName().toString());

                ModInstance latest = mod.getLatestInstance();
                try {
                    var instanceIterator = mod.getInstances().iterator();
                    while (instanceIterator.hasNext()) {
                        ModInstance instance = instanceIterator.next();
                        if (latest.equals(instance)) continue;

                        if (instance.getType() == latest.getType()) {
                            Files.copy(latest.getPath(), instance.getPath(), StandardCopyOption.REPLACE_EXISTING);
                            instanceIterator.remove();
                        }
                    }
                    installed.add(mod);
                    printf("%s installed successfully.", mod.getName());
                } catch (IOException e) {
                    printf("Error while installing %s:", mod.getName());
                    e.printStackTrace();
                    notInstalled.add(mod);
                }
            } else {
                printf("Error while downloading %s:", mod.getName());
                printError(TAB, result);
                notInstalled.add(mod);
            }
        }

        printf("Installed (%d):", installed.size());
        for (Mod mod : installed) {
            println(TAB + mod.getName());
        }

        printf("Not installed (%d):", notInstalled.size());
        for (Mod mod : notInstalled) {
            printf("%s%s (%s)", TAB, mod.getName(), mod.getDownloader().getName());
        }
    }


    // Utils
    private boolean isInvalidUrl(@NotNull String url) {
        try {
            new URL(url);
            return false;
        } catch (MalformedURLException e) {
            return true;
        }
    }

    private @Nullable Mod findMod(@NotNull String name) {
        Mod mod = updater.findMod(name);
        if (mod == null) {
            printf("Mod '%s' not found.", name);
            return null;
        }
        return mod;
    }

    private @Nullable Path buildPath(@NotNull String s) {
        try {
            // Check whether in quotes
            if (s.indexOf('"') == 0 && s.lastIndexOf('"') == s.length() - 1) {
                s = s.substring(1, s.length() - 1);
            }
            Path path = Path.of(s);
            if (Files.isRegularFile(path)) {
                println("'" + path + "' is a regular file.");
                return null;
            }
            return path;
        } catch (InvalidPathException e) {
            printf("Invalid path: %s", s);
            return null;
        }
    }

    private boolean checkArgsLength(@NotNull String commandName, int argsLength, int neededArgs) {
        if (argsLength < neededArgs) {
            printf(TAB + "Requires %s argument%s, got: %s", neededArgs, neededArgs > 1 ? "s" : "", argsLength);
            printf(TAB + "Usage: " + commands.get(commandName.toLowerCase()).getUsage());
            return true;
        }
        return false;
    }

    private @NotNull String input() {
        System.out.print(COMMAND_INLINE);
        return scanner.nextLine();
    }


    // Printing out
    private static void println(@NotNull String... messages) {
        if (messages.length == 0) {
            sleep();
            System.out.println();
        } else {
            for (String line : messages) {
                sleep();
                System.out.println(line);
            }
        }
    }

    private static void println(@Nullable String message) {
        if (message == null) {
            println("null");
        } else {
            println(message.split("\n"));
        }
    }

    private static void printf(@NotNull String format, @NotNull Object... args) {
        println(format.formatted(args));
    }

    private static void printError(String prefix, DownloadResult result) {
        if (result.isSuccess()) return;

        for (String s : result.getMessage()) {
            println(prefix + s);
        }
        var exception = result.getException();
        if (exception != null) {
            println(prefix + exception.getMessage());
            for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
                println(prefix + stackTraceElement.toString());
            }
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
