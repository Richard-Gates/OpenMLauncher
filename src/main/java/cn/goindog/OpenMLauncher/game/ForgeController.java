package cn.goindog.OpenMLauncher.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class Forge_Vanilla {
    private static final String manifest_url = "https://piston-meta.mojang.com/mc/game/version_manifest.json";
    public JsonArray getAllGameVersion() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(manifest_url).openConnection();
        connection.connect();
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder resp = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                resp.append(line);
                resp.append("\n\r");
            }
            JsonArray arr = new Gson().fromJson(resp.toString(), JsonObject.class).getAsJsonArray("versions");
            return arr;
        }
        return null;
    }

    public void Get(String versionName,String pathName) throws IOException {
        System.out.println("[INFO]Reading version_manifest.json file.");
        JsonArray version_manifest_arr = getAllGameVersion();
        for (int vm_index = 0; vm_index < version_manifest_arr.size(); vm_index++) {
            if (Objects.equals(version_manifest_arr.get(vm_index).getAsJsonObject().get("id").getAsString(), versionName)) {
                String gameJsonUrl = version_manifest_arr.get(vm_index).getAsJsonObject().get("url").getAsString();
                PrivateCenterDownload(new URL(gameJsonUrl), pathName);
            }
        }
    }

    private static void PrivateCenterDownload(URL versionJson, String pathName) throws IOException {
        JsonObject versionJsonObj = new Gson().fromJson(IOUtils.toString(versionJson, StandardCharsets.UTF_8), JsonObject.class);
        System.out.println("[INFO]Downloading client.jar file.");
        var clientJarUrl = versionJsonObj.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString();
        var versionDir = System.getProperty("oml.gameDir") + "/versions/" + pathName;
        FileUtils.writeByteArrayToFile(new File(versionDir + "/client.jar"), IOUtils.toByteArray(new URL(clientJarUrl)));
        String assetsIndexUrl = versionJsonObj.getAsJsonObject("assetIndex").get("url").getAsString();
        Thread asset_download = new Thread(() -> {
            try {
                PrivateAssetDownload(new URL(assetsIndexUrl), versionDir + "/assets/");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Thread lib_download = new Thread(() -> {
            System.out.println("[INFO]Libraries download thread started.");
            JsonArray libraries = versionJsonObj.getAsJsonArray("libraries");
            String path = versionDir + "/libraries/";
            PrivateLibrariesDownload(libraries, path);
        });
        asset_download.start();
        lib_download.start();
    }

    private static void PrivateAssetDownload(URL asset_index, String assetPath) throws IOException {
        JsonObject asset_json_obj = new Gson().fromJson(IOUtils.toString(asset_index, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("objects");
        JsonArray asset_json_keys = new Gson().fromJson("[]", JsonArray.class);
        for (String s : asset_json_obj.keySet()) {
            asset_json_keys.add(s);
        }
        Thread th1 = new Thread(() -> {
            for (int i = 0; i < asset_json_keys.size() / 8; i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Downloading asset:" + key);
                try {
                    FileUtils.writeByteArrayToFile(new File(path), IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread th2 = new Thread(() -> {
            for (int i = asset_json_keys.size() / 8; i < asset_json_keys.size() / 4; i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Downloading asset:" + key);
                try {
                    FileUtils.writeByteArrayToFile(new File(path), IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread th3 = new Thread(() -> {
            for (int i = asset_json_keys.size() / 4; i < asset_json_keys.size() / 8 * 3; i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Downloading asset:" + key);
                try {
                    FileUtils.writeByteArrayToFile(new File(path), IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread th4 = new Thread(() -> {
            for (int i = asset_json_keys.size() / 8 * 3; i < asset_json_keys.size() / 2; i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Downloading asset:" + key);
                try {
                    FileUtils.writeByteArrayToFile(new File(path), IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread th5 = new Thread(() -> {
            for (int i = asset_json_keys.size() / 2; i < asset_json_keys.size() / 8 * 5; i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Downloading asset:" + key);
                try {
                    FileUtils.writeByteArrayToFile(new File(path), IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread th6 = new Thread(() -> {
            for (int i = asset_json_keys.size() / 8 * 5; i < asset_json_keys.size() / 4 * 3; i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Downloading asset:" + key);
                try {
                    FileUtils.writeByteArrayToFile(new File(path), IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread th7 = new Thread(() -> {
            for (int i = asset_json_keys.size() / 4 * 3; i < asset_json_keys.size() / 8 * 7; i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Downloading asset:" + key);
                try {
                    FileUtils.writeByteArrayToFile(new File(path), IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread th8 = new Thread(() -> {
            for (int i = asset_json_keys.size() / 8 * 7; i < asset_json_keys.size(); i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Downloading asset:" + key);
                try {
                    FileUtils.writeByteArrayToFile(new File(path), IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        th1.start();
        th2.start();
        th3.start();
        th4.start();
        th5.start();
        th6.start();
        th7.start();
        th8.start();
    }

    private static URL PrivateSummonAssetDownloadUrl(String assetHash) throws MalformedURLException {
        String url = "https://resources.download.minecraft.net/" + assetHash.substring(0, 2) + "/" + assetHash;
        return new URL(url);
    }

    private static void PrivateLibrariesDownload(JsonArray libraries, String libDirPath) {
        Thread download_th1 = new Thread(() -> {
            for (int i = 0; i < libraries.size() / 4; i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.has("artifact")) {
                    String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    System.out.println("[INFO]Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (downloads_obj.has("classifiers")) {
                    String relativePath = "";
                    String url = "";
                    if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("MacOS") && downloads_obj.getAsJsonObject("classifiers").has("natives-osx")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                    }
                    String absolutePath = libDirPath + "/" + relativePath;
                    System.out.println("[INFO]Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
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
        });
        Thread download_th2 = new Thread(() -> {
            for (int i = libraries.size() / 4; i < libraries.size() / 2; i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.has("artifact")) {
                    String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    System.out.println("[INFO]Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (downloads_obj.has("classifiers")) {
                    String relativePath = "";
                    String url = "";
                    if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("MacOS") && downloads_obj.getAsJsonObject("classifiers").has("natives-osx")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                    }
                    String absolutePath = libDirPath + "/" + relativePath;
                    System.out.println("[INFO]Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
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
        });
        Thread download_th3 = new Thread(() -> {
            for (int i = libraries.size() / 2; i < libraries.size() / 4 * 3; i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.has("artifact")) {
                    String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    System.out.println("[INFO]Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (downloads_obj.has("classifiers")) {
                    String relativePath = "";
                    String url = "";
                    if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("MacOS") && downloads_obj.getAsJsonObject("classifiers").has("natives-osx")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                    }
                    String absolutePath = libDirPath + "/" + relativePath;
                    System.out.println("[INFO]Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
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
        });
        Thread download_th4 = new Thread(() -> {
            for (int i = libraries.size() / 4 * 3; i < libraries.size(); i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.has("artifact")) {
                    String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    System.out.println("[INFO]Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (downloads_obj.has("classifiers")) {
                    String relativePath = "";
                    String url = "";
                    if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("MacOS") && downloads_obj.getAsJsonObject("classifiers").has("natives-osx")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                    }
                    String absolutePath = libDirPath + "/" + relativePath;
                    System.out.println("[INFO]Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
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
        });
        download_th1.start();
        download_th2.start();
        download_th3.start();
        download_th4.start();
    }
}

public class ForgeController {
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

    public void Get(@NotNull ForgeInstallerConfig config) throws IOException {
        String forgeVer = config.getForgeVer();
        String mcVer = config.getGameVer();
        JsonObject versions = getAllForgeVersion();
        if (versions.has(mcVer)) {
            JsonArray versionArr = versions.getAsJsonArray(mcVer);
            for (JsonElement je : versionArr) {
                String forgeVersion = je.getAsJsonObject().get("version").getAsString();
                if (forgeVersion.equals(forgeVer)) {
                    PrivateCenterDownload(config);
                }
            }
        }
    }

    private static void PrivateCenterDownload(ForgeInstallerConfig conf) throws IOException {
        Thread installer_download = new Thread(() -> {
            PrivateInstallerDownload(conf);
            try {
                PrivateUnpackInstaller(new File(System.getProperty("oml.gameDir") + "/versions/" + conf.getGameVer() + "-forge-" + conf.getForgeVer()), conf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Thread vanilla_install = new Thread(() -> {
            try {
                new Forge_Vanilla().Get(conf.getGameVer(), conf.getGameVer() + "-forge-" + conf.getForgeVer());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        installer_download.start();
        vanilla_install.start();
    }

    private static void PrivateInstallerDownload(@NotNull ForgeInstallerConfig conf) {
        String installJarPath = System.getProperty("oml.gameDir") + "/versions/" + conf.getGameVer() + "-forge-" + conf.getForgeVer() + "/forge-installer.jar";
        File installJarFile = new File(installJarPath);
        URL installJarUrl = null;
        try {
            installJarUrl = new URL("https://bmclapi2.bangbang93.com/forge/download/?mcversion=" + conf.getGameVer() + "&version=" + conf.getForgeVer() + "&category=installer&format=jar");
            FileUtils.writeByteArrayToFile(installJarFile, IOUtils.toByteArray(installJarUrl));
            System.out.println("[INFO]Downloading Installer");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void PrivateUnpackInstaller(@NotNull File gameDir, ForgeInstallerConfig conf) throws IOException {
        JarFile installer = new JarFile(new File(gameDir.getAbsolutePath() + "/forge-installer.jar"));
        for (Enumeration<JarEntry> enums = installer.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();
            File f = new File(gameDir.getAbsolutePath() + File.separator + "unpack/" + entry.getName());
            if (entry.isDirectory()) {
                f.mkdirs();
            } else {
                InputStream inputStream = installer.getInputStream(entry);
                byte[] b = new byte[inputStream.available()];
                while (inputStream.available() > 0) {
                    inputStream.read(b);
                }
                inputStream.close();
                FileUtils.writeByteArrayToFile(f, b);
            }
        }
        PrivateAutomaticDownload(new File(gameDir.getAbsolutePath() + File.separator + "unpack/"), conf);
    }

    private static void PrivateAutomaticDownload(File unpackDir, ForgeInstallerConfig conf) throws IOException {
        String unpackDirStr = unpackDir.getAbsolutePath();
        String install_profile = FileUtils.readFileToString(new File(unpackDirStr + File.separator + "install_profile.json"), StandardCharsets.UTF_8);
        JsonObject install_profile_obj = new Gson().fromJson(install_profile, JsonObject.class);
        String version_json_relative_path = install_profile_obj.get("json").getAsString();
        String version_profile = FileUtils.readFileToString(new File(unpackDirStr + File.separator + version_json_relative_path), StandardCharsets.UTF_8);
        JsonObject version_profile_obj = new Gson().fromJson(version_profile, JsonObject.class);
        String libDirPath = unpackDirStr.replace("unpack", "libraries");
        FileUtils.writeStringToFile(new File(unpackDirStr.replace("unpack", conf.getGameVer() + "-forge-" + conf.getForgeVer() + ".json")), version_profile, StandardCharsets.UTF_8);
        PrivateLibrariesDownload(version_profile_obj, libDirPath, conf);
    }

    private static void PrivateLibrariesDownload(JsonObject version_obj, String libDirPath, ForgeInstallerConfig conf) {
        JsonArray libraries = version_obj.getAsJsonArray("libraries");
        Thread download_th1 = new Thread(() -> {
            for (int i = 0; i < libraries.size() / 4; i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.get("name").getAsString().contains("net.minecraftforge:forge:")) {
                    String forgePath = downloads_obj.get("downloads").getAsJsonObject().get("artifact").getAsJsonObject().get("path").getAsString();
                    try {
                        FileUtils.copyFile(new File(libDirPath.replace("libraries", "unpack/") + forgePath), new File(libDirPath.replace("/libraries", "/") + "forge-" + conf.getGameVer() + "-" + conf.getForgeVer() + ".jar"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    if (downloads_obj.has("artifact")) {
                        String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                        String absolutePath = libDirPath + "/" + relativePath;
                        String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                        System.out.println("[INFO]Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                        try {
                            FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (downloads_obj.has("classifiers")) {
                        String relativePath = "";
                        String url = "";
                        if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                            relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                            url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                        } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                            relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                            url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                        } else if (System.getProperty("os.name").contains("MacOS") && downloads_obj.getAsJsonObject("classifiers").has("natives-osx")) {
                            relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                            url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                        }
                        String absolutePath = libDirPath + "/" + relativePath;
                        System.out.println("[INFO]Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                        try {
                            FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
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
        });
        Thread download_th2 = new Thread(() -> {
            for (int i = libraries.size() / 4; i < libraries.size() / 2; i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.has("artifact")) {
                    String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    System.out.println("[INFO]Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (downloads_obj.has("classifiers")) {
                    String relativePath = "";
                    String url = "";
                    if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("MacOS") && downloads_obj.getAsJsonObject("classifiers").has("natives-osx")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                    }
                    String absolutePath = libDirPath + "/" + relativePath;
                    System.out.println("[INFO]Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
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
        });
        Thread download_th3 = new Thread(() -> {
            for (int i = libraries.size() / 2; i < libraries.size() / 4 * 3; i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.has("artifact")) {
                    String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    System.out.println("[INFO]Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (downloads_obj.has("classifiers")) {
                    String relativePath = "";
                    String url = "";
                    if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("MacOS") && downloads_obj.getAsJsonObject("classifiers").has("natives-osx")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                    }
                    String absolutePath = libDirPath + "/" + relativePath;
                    System.out.println("[INFO]Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
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
        });
        Thread download_th4 = new Thread(() -> {
            for (int i = libraries.size() / 4 * 3; i < libraries.size(); i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.has("artifact")) {
                    String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    System.out.println("[INFO]Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (downloads_obj.has("classifiers")) {
                    String relativePath = "";
                    String url = "";
                    if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                    } else if (System.getProperty("os.name").contains("MacOS") && downloads_obj.getAsJsonObject("classifiers").has("natives-osx")) {
                        relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("path").getAsString();
                        url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-osx").get("url").getAsString();
                    }
                    String absolutePath = libDirPath + "/" + relativePath;
                    System.out.println("[INFO]Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
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
        });
        download_th1.start();
        download_th2.start();
        download_th3.start();
        download_th4.start();
    }

    public static void main(String[] args) throws IOException {
        ForgeInstallerConfig config = new ForgeInstallerConfig();
        config.setForgeVer("36.2.39");
        config.setGameVer("1.16.5");
        new ForgeController().Get(config);
    }
}
