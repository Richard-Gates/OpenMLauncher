package cn.goindog.OpenMLauncher.game.download.Vanilla;

import cn.goindog.OpenMLauncher.events.GameEvents.DownloadFinishEvent;
import cn.goindog.OpenMLauncher.events.GameEvents.DownloadFinishEventListener;
import cn.goindog.OpenMLauncher.events.OAuthEvents.OAuthFinishEvent;
import cn.goindog.OpenMLauncher.events.OAuthEvents.OAuthFinishEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.jar.JarFile;

public class VanillaDownloader {
    private static final String manifest_url = "https://piston-meta.mojang.com/mc/game/version_manifest.json";
    public String gameDir = System.getProperty("user.dir") + "/.minecraft";
    private static String verName = "";
    private static String gameDownloadDir = System.getProperty("oml.gameDir") + "/versions/" + verName;

    private Collection listeners;

    public void addDownloadFinishListener(DownloadFinishEventListener listener) {
        if (listeners == null) {
            listeners = new HashSet();
        }
        listeners.add(listener);
    }

    public void removeDownloadFinishListener(DownloadFinishEventListener listener) {
        if (listeners == null) return;
        listeners.remove(listener);
    }

    protected void fireWorkspaceStarted(String type) {
        if (listeners == null) return;
        DownloadFinishEvent event = new DownloadFinishEvent(this, type);
        notifyListeners(event);
    }

    private void notifyListeners(DownloadFinishEvent event) {
        for (Object o : listeners) {
            DownloadFinishEventListener listener = (DownloadFinishEventListener) o;
            listener.DownloadFinishEvent(event);
        }
    }

    public VanillaDownloader setGameDownloadDir(String newGameDownloadDir) {
        gameDownloadDir = newGameDownloadDir;
        return this;
    }

    public static String getGameDownloadDir() {
        return gameDownloadDir;
    }

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

    public void build(VanillaInstallProfile profile) throws IOException {
        verName = profile.getVersionName();
        gameDownloadDir = System.getProperty("oml.gameDir") + "/versions/" + verName;
        String versionName = profile.getVersion();
        System.out.println("[INFO]Vanilla Downloader: Reading version_manifest.json file.");
        JsonArray version_manifest_arr = getAllGameVersion();
        for (int vm_index = 0; vm_index < version_manifest_arr.size(); vm_index++) {
            if (Objects.equals(version_manifest_arr.get(vm_index).getAsJsonObject().get("id").getAsString(), versionName)) {
                String gameJsonUrl = version_manifest_arr.get(vm_index).getAsJsonObject().get("url").getAsString();
                PrivateCenterDownload(new URL(gameJsonUrl), profile);
            }
        }
    }

    private void PrivateCenterDownload(URL versionJson, VanillaInstallProfile profile) throws IOException {
        JsonObject versionJsonObj = new Gson().fromJson(IOUtils.toString(versionJson, StandardCharsets.UTF_8), JsonObject.class);
        PrivateLog4jConfigBuild(versionJsonObj);
        System.out.println("[INFO]Vanilla Downloader: Downloading client.jar file.");
        var clientJarUrl = versionJsonObj.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString();
        var versionDir = gameDownloadDir;
        FileUtils.writeByteArrayToFile(new File(versionDir + File.separator + profile.getVersion() + ".jar"), IOUtils.toByteArray(new URL(clientJarUrl)));
        String assetsIndexUrl = versionJsonObj.getAsJsonObject("assetIndex").get("url").getAsString();
        Thread asset_download = new Thread(() -> {
            try {
                PrivateAssetDownload(new URL(assetsIndexUrl), versionDir + "/assets/objects/", versionJsonObj.getAsJsonObject("assetIndex").get("id").getAsString(), versionJsonObj.getAsJsonArray("libraries"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Thread writeGameJsonThread = new Thread(() -> {
            try {
                FileUtils.writeByteArrayToFile(new File(versionDir + "/" + verName + ".json"), IOUtils.toByteArray(versionJson));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        asset_download.start();
        writeGameJsonThread.start();
    }

    private void PrivateAssetDownload(URL asset_index, String assetPath, String AssetsId, JsonArray libraries) throws IOException {
        JsonObject asset_json_obj = new Gson().fromJson(IOUtils.toString(asset_index, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("objects");
        JsonArray asset_json_keys = new Gson().fromJson("[]", JsonArray.class);
        for (String s : asset_json_obj.keySet()) {
            asset_json_keys.add(s);
        }
        File assets = new File(gameDownloadDir + File.separator + "assets" + File.separator + "indexes" + File.separator + AssetsId + ".json");
        FileUtils.writeByteArrayToFile(assets, IOUtils.toByteArray(asset_index));
        Thread th1 = new Thread(() -> {
            for (int i = 0; i < asset_json_keys.size() / 8; i++) {
                String key = asset_json_keys.get(i).getAsString();
                String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;
                System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
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
                System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
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
                System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
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
                System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
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
                System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
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
                System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
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
                System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
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
                System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
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

        System.out.println("[INFO]Vanilla Downloader: Libraries download thread started.");
        String path = gameDownloadDir + "/libraries/";
        PrivateLibrariesDownload(libraries, path);
    }

    public void reDownloadFiles(URL assetsIndex, String assetsPath, String assetsId, JsonArray libraries) throws IOException {
        PrivateAssetDownload(assetsIndex, assetsPath, assetsId, libraries);
    }

    private static URL PrivateSummonAssetDownloadUrl(String assetHash) throws MalformedURLException {
        String url = "https://resources.download.minecraft.net/" + assetHash.substring(0, 2) + "/" + assetHash;
        return new URL(url);
    }

    private void PrivateLibrariesDownload(JsonArray libraries, String libDirPath) {
        Thread download_th1 = new Thread(() -> {
            for (int i = 0; i < libraries.size() / 4; i++) {
                JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                if (downloads_obj.has("artifact")) {
                    String relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    String url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    System.out.println("[INFO]Vanilla Downloader: Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
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
                    System.out.println("[INFO]Vanilla Downloader: Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
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
                            FileUtils.writeByteArrayToFile(new File(nativeDir + File.separator + jarEntry.getName()), IOUtils.toByteArray(inputStream));
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
                    System.out.println("[INFO]Vanilla Downloader: Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
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
                    System.out.println("[INFO]Vanilla Downloader: Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
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
                            FileUtils.writeByteArrayToFile(new File(nativeDir + File.separator + jarEntry.getName()), IOUtils.toByteArray(inputStream));
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
                    System.out.println("[INFO]Vanilla Downloader: Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                    try {
                        FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                String relativePath = "";
                String url = "";
                if (downloads_obj.has("classifiers")) {
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
                    System.out.println("[INFO]Vanilla Downloader: Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
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
                            FileUtils.writeByteArrayToFile(new File(nativeDir + File.separator + jarEntry.getName()), IOUtils.toByteArray(inputStream));
                            inputStream.close();
                        }
                        jarFile.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (downloads_obj.getAsJsonObject("artifact").get("path").getAsString().contains("natives-" + System.getProperty("os.name")) && downloads_obj.getAsJsonObject("artifact").get("path").getAsString().contains(System.getProperty("os.arch"))) {
                    relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                    url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                    String absolutePath = libDirPath + "/" + relativePath;
                    System.out.println("[INFO]Vanilla Downloader: Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
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
                            FileUtils.writeByteArrayToFile(new File(nativeDir + File.separator + jarEntry.getName()), IOUtils.toByteArray(inputStream));
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
                    System.out.println("[INFO]Vanilla Downloader: Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
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
                    System.out.println("[INFO]Vanilla Downloader: Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
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
                            FileUtils.writeByteArrayToFile(new File(nativeDir + File.separator + jarEntry.getName()), IOUtils.toByteArray(inputStream));
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

    private void PrivateLog4jConfigBuild(JsonObject versionJson) throws IOException {
        JsonObject client = versionJson.getAsJsonObject("logging").getAsJsonObject("client");
        String url = client.getAsJsonObject("file").get("url").getAsString();
        String id = client.getAsJsonObject("file").get("id").getAsString();
        File config = new File(gameDownloadDir + File.separator + "log4j" + File.separator + id);
        FileUtils.writeByteArrayToFile(config, IOUtils.toByteArray(new URL(url)));
    }
}
