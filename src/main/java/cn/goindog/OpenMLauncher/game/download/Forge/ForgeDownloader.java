package cn.goindog.OpenMLauncher.game.download.Forge;

import cn.goindog.OpenMLauncher.exceptions.ForgeExceptions.NullInstallerException;
import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaDownloader;
import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaInstallProfile;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ForgeDownloader {
    public JsonObject getAllForgeVersion() throws IOException {
        String canUseForgeVersionStr = IOUtils.toString(new URL("https://bmclapi2.bangbang93.com/forge/minecraft"));
        JsonArray canUseForgeVersionArr = new Gson().fromJson(canUseForgeVersionStr, JsonArray.class);
        JsonObject obj = new Gson().fromJson("{}", JsonObject.class);
        for (JsonElement je : canUseForgeVersionArr) {
            String s = je.getAsString();
            URL versionForgeListUrl = new URL("https://bmclapi2.bangbang93.com/forge/minecraft/" + s);
            obj.add(s, new Gson().fromJson(IOUtils.toString(versionForgeListUrl), JsonArray.class));
        }
        return obj;
    }

    public void build(ForgeInstallProfile profile) throws IOException {

        String forgeVer = profile.getForgeVer();
        String mcVer = profile.getGameVer();
        JsonObject versions = getAllForgeVersion();
        if (versions.has(mcVer)) {
            JsonArray versionArr = versions.getAsJsonArray(mcVer);
            for (JsonElement je : versionArr) {
                String forgeVersion = je.getAsJsonObject().get("version").getAsString();
                if (forgeVersion.equals(forgeVer)) {
                    PrivateCenterDownload(profile);
                }
            }
        }
    }

    private static void PrivateCenterDownload(ForgeInstallProfile conf) {
        Thread installer_download = new Thread(() -> {
            PrivateInstallerDownload(conf);
            String path = System.getProperty("oml.gameDir") + "/versions/" + conf.getGameVer() + "-forge-" + conf.getForgeVer() + "/";

            try {
                new VanillaDownloader().setGameDownloadDir(path).build(
                        new VanillaInstallProfile().setVersion(conf.getGameVer()).setVersionName(conf.getGameVer() + "-forge-" + conf.getForgeVer())
                );
                File installer = new File(path + "/forge-installer.jar");
                PrivateInstall(installer, conf.getGameVer());
            } catch (IOException | InterruptedException | NullInstallerException e) {
                throw new RuntimeException(e);
            }
        });
        installer_download.start();
    }

    private static void PrivateInstallerDownload(ForgeInstallProfile conf) {
        String installJarPath = System.getProperty("oml.gameDir") + "/versions/" + conf.getGameVer() + "-forge-" + conf.getForgeVer() + "/forge-installer.jar";
        File installJarFile = new File(installJarPath);
        URL installJarUrl;
        try {
            installJarUrl = new URL("https://bmclapi2.bangbang93.com/forge/download/?mcversion=" + conf.getGameVer() + "&version=" + conf.getForgeVer() + "&category=installer&format=jar");
            FileUtils.writeByteArrayToFile(installJarFile, IOUtils.toByteArray(installJarUrl));
            System.out.println("[INFO]Downloading Installer");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void PrivateInstall(File Installer, String mcVersion) throws IOException, InterruptedException, NullInstallerException {
        String[] versionList = mcVersion.split("\\.");
        if (Integer.parseInt(versionList[1]) <= 13) {
            new ForgeOldInstaller(Installer).build();
        } else {
            new ForgeNewInstaller(Installer).build();
        }
    }

    private static void createLauncherProfile() throws IOException {
        JsonObject obj = new Gson().fromJson("{}", JsonObject.class);
        JsonObject profilesObj = new Gson().fromJson("{}", JsonObject.class);
        JsonObject profileDefault = new Gson().fromJson("{}", JsonObject.class);
        profileDefault.addProperty("gameDir", System.getProperty("oml.gameDir"));
        profileDefault.addProperty("lastVersionId", "1.20.1");
        profileDefault.addProperty("name", "(Default)");
        profilesObj.add("(Default)", profileDefault);
        obj.add("profile", profilesObj);
        String profileDir = System.getProperty("oml.gameDir") + File.separator + "launcher_profiles.json";
        FileUtils.writeStringToFile(new File(profileDir), profilesObj.toString(), StandardCharsets.UTF_8);
    }

    static class ForgeOldInstaller {
        private File installer;

        public ForgeOldInstaller(File installer) {
            this.installer = installer;
        }

        public ForgeOldInstaller() {
        }

        public ForgeOldInstaller setInstaller(File installer) {
            this.installer = installer;
            return this;
        }

        public File getInstaller() {
            return this.installer;
        }


        private void buildTemp() throws IOException {
            JarFile installer = new JarFile(this.getInstaller());
            for (Enumeration<JarEntry> entryEnumeration = installer.entries(); entryEnumeration.hasMoreElements(); ) {
                JarEntry entry = entryEnumeration.nextElement();

                String path = getInstaller().getPath().replace("forge-installer.jar", "temp/" + entry.getName());
                File unJarFile = new File(path);

                if (entry.isDirectory()) {
                    unJarFile.mkdirs();
                    continue;
                }

                InputStream stream = installer.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(unJarFile);
                while (stream.available() > 0) {
                    fos.write(stream.read());
                }

                fos.close();
                stream.close();
            }
            librariesDownload();
        }


        private void removeVersionJson(String versionName) throws IOException {
            FileUtils.copyFile(
                    new File(
                            getInstaller().getPath().replace("forge-installer.jar", "temp/version.json")
                    ),
                    new File(
                            getInstaller().getPath().replace("forge-installer.jar", versionName + ".json")
                    )
            );
        }

        private void librariesDownload() throws IOException {
            JsonObject versionJson = new Gson().fromJson(
                    FileUtils.readFileToString(
                            new File(
                                    getInstaller().getPath().replace("forge-installer.jar", "temp/version.json")
                            ),
                            StandardCharsets.UTF_8
                    ),
                    JsonObject.class
            );
            JsonArray libraries = versionJson.getAsJsonArray("libraries");
            String versionName = "";
            for (JsonElement library : libraries) {
                String path = library.getAsJsonObject()
                        .get("downloads").getAsJsonObject()
                        .get("artifact").getAsJsonObject()
                        .get("path").getAsString();
                String url = library.getAsJsonObject()
                        .get("downloads").getAsJsonObject()
                        .get("artifact").getAsJsonObject()
                        .get("url").getAsString();
                String name = library.getAsJsonObject().get("name").getAsString();
                System.out.println("[INFO]OldForge Builder: Downloading libraries:" + name);
                if (!path.contains("net/minecraftforge/forge/")) {
                    FileUtils.writeByteArrayToFile(
                            new File(
                                    getInstaller().getPath().replace(
                                            "forge-installer.jar",
                                            "libraries/" + path)
                            ),
                            IOUtils.toByteArray(
                                    new URL(url)
                            )
                    );
                } else {
                    if (
                            library.getAsJsonObject().get("name").getAsString()
                                    .contains("net.minecraftforge:forge")
                    ) {
                        String[] packagePath = library.getAsJsonObject().get("name").getAsString().split(":");
                        removeVersionJson(packagePath[2].replace("-", "-forge-"));
                        versionName = packagePath[2].replace("-", "-forge-");
                        FileUtils.copyFile(
                                new File(
                                        getInstaller().getPath().replace("forge-installer.jar", "temp/maven/" + path)
                                ),
                                new File(
                                        getInstaller().getPath().replace("forge-installer.jar", "libraries/" + path)
                                )
                        );
                        FileUtils.copyFile(
                                new File(
                                        getInstaller().getPath().replace("forge-installer.jar", packagePath[2].split("-")[0] + ".jar")
                                ),
                                new File(
                                        getInstaller().getPath().replace("forge-installer.jar", versionName + ".jar")
                                )
                        );
                        FileUtils.delete(
                                new File(
                                        getInstaller().getPath().replace("forge-installer.jar", packagePath[2].split("-")[0] + ".jar")
                                )
                        );
                        JsonObject vanillaJson = new Gson().fromJson(
                                FileUtils.readFileToString(
                                        new File(
                                                getInstaller().getPath().replace("forge-installer.jar", packagePath[2].replace("-", "-forge-") + ".json")
                                        ),
                                        StandardCharsets.UTF_8
                                ),
                                JsonObject.class
                        );
                        JsonObject forgeJson = new Gson().fromJson(
                                FileUtils.readFileToString(
                                        new File(
                                                getInstaller().getPath().replace("forge-installer.jar", "temp/version.json")
                                        ),
                                        StandardCharsets.UTF_8
                                ),
                                JsonObject.class
                        );
                        JsonObject mergeJson = versionJsonMerge(forgeJson, vanillaJson);
                        mergeJson.add(
                                "clientVersion",
                                mergeJson.get("inheritsFrom")
                        );
                        FileUtils.writeByteArrayToFile(
                                new File(
                                        getInstaller().getPath().replace("forge-installer.jar", packagePath[2].replace("-", "-forge-") + ".json")
                                ),
                                mergeJson.toString().getBytes()
                        );
                        FileUtils.delete(
                                new File(getInstaller().getPath().replace("forge-installer.jar", packagePath[2].split("-")[0]) + ".json")
                        );
                    }
                }
                if (library.getAsJsonObject().has("classifiers")) {
                    String libDirPath = System.getProperty("oml.gameDir") + File.separator + "libraries" + File.separator;
                    String relativePath = "";
                    String nativeUrl = "";
                    if (System.getProperty("os.name").contains("Windows") && library.getAsJsonObject().getAsJsonObject("classifiers").has("natives-windows")) {
                        relativePath = library.getAsJsonObject().getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                        nativeUrl = library.getAsJsonObject().getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("Linux") && library.getAsJsonObject().getAsJsonObject("classifiers").has("natives-linux")) {
                        relativePath = library.getAsJsonObject().getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                        nativeUrl = library.getAsJsonObject().getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("MacOS") && library.getAsJsonObject().getAsJsonObject("classifiers").has("natives-osx")) {
                        relativePath = library.getAsJsonObject().getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                        nativeUrl = library.getAsJsonObject().getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                    }
                    String absolutePath = libDirPath + "/" + relativePath;
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(nativeUrl)));
                        var nativeDir = libDirPath.replace("/libraries", "/natives/");
                        var jarFile = new JarFile(absolutePath);
                        var entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            var jarEntry = entries.nextElement();
                            if (jarEntry.isDirectory() || jarEntry.getName().contains("META-INF")) {
                                continue;
                            }

                            var inputStream = jarFile.getInputStream(jarEntry);
                            FileUtils.writeByteArrayToFile(new File(nativeDir, jarEntry.getName()), IOUtils.toByteArray(inputStream));
                            inputStream.close();
                        }
                        jarFile.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }


        private void deleteTemp(String versionName) throws IOException {
            File temp = new File(
                    getInstaller().getPath().replace("forge-installer.jar", "temp/")
            );

            FileUtils.delete(getInstaller());
            FileUtils.deleteDirectory(temp);

            String[] version = versionName.split("-");
            FileUtils.copyFile(
                    new File(
                            getInstaller().getPath().replace("forge-installer.jar", version[0] + ".jar")
                    ),
                    new File(
                            getInstaller().getPath().replace("forge-installer.jar", versionName + ".jar")
                    )
            );
            FileUtils.delete(
                    new File(
                            getInstaller().getPath().replace("forge-installer.jar", version[0] + ".jar")
                    )
            );
        }

        private JsonObject versionJsonMerge(JsonObject firstObj, JsonObject secondObj) {
            for (String key : secondObj.keySet()) {
                if (!firstObj.has(key)) {
                    firstObj.add(key, secondObj.get(key));
                }
            }
            for (JsonElement element : secondObj.getAsJsonArray("libraries")) {
                firstObj.getAsJsonArray("libraries").add(element);
            }
            for (JsonElement element : secondObj.getAsJsonObject("arguments").getAsJsonArray("game")) {
                firstObj.getAsJsonObject("arguments").getAsJsonArray("game").add(element);
            }
            return firstObj;
        }

        public void build() throws NullInstallerException, IOException {
            if (this.installer == null) {
                throw new NullInstallerException("The installer is not valid.Please check forge-installer path.");
            }

            this.buildTemp();
        }
    }

    static class ForgeNewInstaller {
        private File installer;

        public ForgeNewInstaller() throws IOException {

        }

        public ForgeNewInstaller(File installer) throws IOException {
            this.installer = installer;
        }

        public ForgeNewInstaller setInstaller(File installer) {
            this.installer = installer;
            return this;
        }

        public File getInstaller() {
            return this.installer;
        }

        private JsonObject versionJsonMerge(JsonObject forgeVersionJson, JsonObject vanillaVersionJson) {
            for (String key : vanillaVersionJson.keySet()) {
                if (!forgeVersionJson.has(key)) {
                    forgeVersionJson.add(key, vanillaVersionJson.get(key));
                }
            }
            for (JsonElement element : vanillaVersionJson.getAsJsonArray("libraries")) {
                forgeVersionJson.getAsJsonArray("libraries").add(element);
            }
            for (JsonElement element : vanillaVersionJson.getAsJsonObject("arguments").getAsJsonArray("game")) {
                forgeVersionJson.getAsJsonObject("arguments").getAsJsonArray("game").add(element);
            }
            if ( vanillaVersionJson.getAsJsonObject("arguments").has("jvm")) {
                for (JsonElement element : vanillaVersionJson.getAsJsonObject("arguments").getAsJsonArray("jvm")) {
                    forgeVersionJson.getAsJsonObject("arguments").getAsJsonArray("jvm").add(element);
                }
            }
            forgeVersionJson.add("clientVersion", forgeVersionJson.get("inheritsFrom"));
            forgeVersionJson.remove("inheritsFrom");
            return forgeVersionJson;
        }

        public void build() throws NullInstallerException, IOException {
            if (this.installer == null) {
                throw new NullInstallerException("The installer is not valid.Please check forge-installer path.");
            }

            PrivateInstallerToolDownload();
        }

        private void PrivateInstallerToolDownload() throws IOException {
            FileUtils.writeByteArrayToFile(
                    new File(
                            this.installer.getAbsolutePath().replace("forge-installer.jar", "forge-installer-tool.jar")
                    ),
                    IOUtils.toByteArray(
                            new URL("https://github.com/Kamesuta/ForgeInstallerCLI/releases/download/1.0.1/ForgeInstallerCLI-1.0.1.jar")
                    )
            );
            PrivateInstall(
                    new File(
                            this.installer.getAbsolutePath().replace("forge-installer.jar", "forge-installer-tool.jar")
                    )
            );
        }

        private void PrivateInstall(File cli_jar) {
            StringBuilder installCommand = new StringBuilder();
            installCommand.append("java -jar \"")
                    .append(cli_jar.getAbsolutePath())
                    .append("\" --installer \"")
                    .append(this.installer.getAbsolutePath())
                    .append("\" --target \"")
                    .append(System.getProperty("oml.gameDir"))
                    .append("\" --progress");

            try {
                Process process = Runtime.getRuntime().exec(installCommand.toString());
                String input = consumeInputStream(process, "medium");
                String errStr = consumeInputStream(process, "error");
                process.waitFor();
                MergeJson();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void MergeJson() throws IOException {
            String separator = File.separator;
            String[] forgeJsonPathList = getInstaller().getAbsolutePath().split("\\" + separator);
            String forgeVersionPath = getInstaller().getAbsolutePath().replace("forge-installer.jar", forgeJsonPathList[forgeJsonPathList.length - 2] + ".json");
            JsonObject forgeVersionJson = new Gson().fromJson(
                    FileUtils.readFileToString(
                            new File(
                                    forgeVersionPath
                            ),
                            StandardCharsets.UTF_8
                    ),
                    JsonObject.class
            );

            String version = forgeVersionJson.get("inheritsFrom").getAsString();

            JsonObject vanillaVersionJson = new Gson().fromJson(
                    FileUtils.readFileToString(
                            new File(
                                    getInstaller().getAbsolutePath().replace("forge-installer.jar", version + ".json")
                            ),
                            StandardCharsets.UTF_8
                    ),
                    JsonObject.class
            );

            JsonObject mergeJson = versionJsonMerge(forgeVersionJson, vanillaVersionJson);

            FileUtils.writeStringToFile(
                    new File(
                            forgeVersionPath
                    ),
                    mergeJson.toString(),
                    StandardCharsets.UTF_8
            );
            removeTemp(version);
        }

        private String consumeInputStream(Process process,String type) throws IOException {
            InputStream is;
            if (type.contains("medium")) {
                is = process.getInputStream();
            } else {
                is = process.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
            String s;
            StringBuilder sb = new StringBuilder();
            while ((s = br.readLine()) != null) {
                System.out.println(s);
                sb.append(s);
            }
            return sb.toString();
        }

        private void removeTemp(String versionName) throws IOException {
            File installer = getInstaller();
            File cli = new File(
                    getInstaller().getAbsolutePath().replace("forge-installer.jar", "forge-installer-tool.jar")
            );
            File vannillaJsonFile = new File(
                    getInstaller().getAbsolutePath().replace("forge-installer.jar", versionName)
            );

            FileUtils.delete(installer);
            FileUtils.delete(cli);
            FileUtils.delete(vannillaJsonFile);
        }
    }
}
