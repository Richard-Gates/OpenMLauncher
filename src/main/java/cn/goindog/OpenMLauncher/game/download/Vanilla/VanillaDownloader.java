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
    private boolean assetsStatus = false;
    private boolean librariesStatus = false;

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
        FileUtils.writeByteArrayToFile(new File(gameDownloadDir + File.separator + profile.getVersionName() + ".jar"), IOUtils.toByteArray(new URL(clientJarUrl)));
        String assetsIndexUrl = versionJsonObj.getAsJsonObject("assetIndex").get("url").getAsString();
        PrivateAssetDownload(
                new URL(assetsIndexUrl),
                System.getProperty("oml.gameDir") + "/assets/objects/",
                versionJsonObj.getAsJsonObject("assetIndex").get("id").getAsString(),
                versionJsonObj.getAsJsonArray("libraries"),
                32
        );
        writeVersionJson(gameDownloadDir, versionJson, profile);
    }

    private void writeVersionJson(String versionDir, URL versionJson, VanillaInstallProfile profile) {
        try {
            FileUtils.writeByteArrayToFile(new File(versionDir + "/" + profile.getVersion() + ".json"), IOUtils.toByteArray(versionJson));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void PrivateAssetDownload(URL asset_index, String assetPath, String AssetsId, JsonArray libraries,int threadTimes) throws IOException {
        JsonObject asset_json_obj = new Gson().fromJson(IOUtils.toString(asset_index, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("objects");
        JsonArray asset_json_keys = new Gson().fromJson("[]", JsonArray.class);
        for (String s : asset_json_obj.keySet()) {
            asset_json_keys.add(s);
        }
        File assets = new File(assetPath.replace("objects", "indexes") + File.separator + AssetsId + ".json");
        FileUtils.writeByteArrayToFile(assets, IOUtils.toByteArray(asset_index));

        final double[] count = {0};


        for (int time = 0;time < threadTimes; time++) {
            int finalTime = time;
            Thread th = new Thread(() -> {
                for (int i = 0; i < asset_json_keys.size() / threadTimes * (finalTime + 1); i++) {
                    String key = asset_json_keys.get(i).getAsString();
                    String hash = asset_json_obj.getAsJsonObject(key).get("hash").getAsString();
                    String path = assetPath + "/" + hash.substring(0, 2) + "/" + hash;

                    try {
                        File assetFile = new File(path);
                        if (!assetFile.exists()) {
                            System.out.println("[INFO]Vanilla Downloader: Downloading asset:" + key);
                            FileUtils.writeByteArrayToFile(assetFile, IOUtils.toByteArray(PrivateSummonAssetDownloadUrl(hash)));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    count[0]++;
                }
            });
            th.start();
        }

        new Thread(() -> {
            while (true) {
                if (count[0] == threadTimes) {
                    System.out.println("[INFO]Vanilla Downloader: Libraries download thread started.");
                    String path = System.getProperty("oml.gameDir") + "/libraries/";
                    PrivateLibrariesDownload(libraries, path, 20);
                }
            }
        }).start();
    }

    public void reDownloadFiles(URL assetsIndex, String assetsPath, String assetsId, JsonArray libraries) throws IOException {
        PrivateAssetDownload(assetsIndex, assetsPath, assetsId, libraries,64);
    }

    private static URL PrivateSummonAssetDownloadUrl(String assetHash) throws MalformedURLException {
        String url = "https://resources.download.minecraft.net/" + assetHash.substring(0, 2) + "/" + assetHash;
        return new URL(url);
    }

    private void PrivateLibrariesDownload(JsonArray libraries, String libDirPath, int threadTimes) {
        final double[] count = {0};
        for (int times = 0; times < threadTimes; times++) {
            int finalTimes = times;
            new Thread(() -> {
                for (int i = 0; i < libraries.size() / threadTimes * (finalTimes + 1); i++) {
                    if (!libraries.get(i).getAsJsonObject().has("downloads")) {
                        String[] packageFull = libraries.get(i).getAsJsonObject().get("name").getAsString().split(":");
                        String packagePath = packageFull[0].replace(".", "/");
                        String packageName = packageFull[1];
                        String packageVersion = packageFull[2];
                        String relativePath = packagePath + "/" + packageName + "/" + packageVersion + "/" + packageName + "-" + packageVersion + ".jar";

                        String host = libraries.get(i).getAsJsonObject().get("url").getAsString();

                        try {
                            FileUtils.writeByteArrayToFile(
                                    new File(
                                            libDirPath
                                                    + File.separator
                                                    + relativePath
                                    ),
                                    IOUtils.toByteArray(
                                    new URL(
                                                    host + relativePath
                                            )
                                    )
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    JsonObject downloads_obj = libraries.get(i).getAsJsonObject().getAsJsonObject("downloads");
                    if (downloads_obj.has("artifact")) {
                        String relativePath = "";
                        String url = "";
                        if (downloads_obj.has("classifiers")) {
                            if (System.getProperty("os.name").contains("Windows") && downloads_obj.getAsJsonObject("classifiers").has("natives-windows")) {
                                relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("path").getAsString();
                                url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-windows").get("url").getAsString();
                                nativeDownload(libDirPath, relativePath, libraries, i, url);
                            } else if (System.getProperty("os.name").contains("Linux") && downloads_obj.getAsJsonObject("classifiers").has("natives-linux")) {
                                relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("path").getAsString();
                                url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-linux").get("url").getAsString();
                                nativeDownload(libDirPath, relativePath, libraries, i, url);
                            } else if (System.getProperty("os.name").contains("Mac OS") && downloads_obj.getAsJsonObject("classifiers").has("natives-macos")) {
                                relativePath = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-macos").get("path").getAsString();
                                url = downloads_obj.getAsJsonObject("classifiers").getAsJsonObject("natives-macos").get("url").getAsString();
                                nativeDownload(libDirPath, relativePath, libraries, i, url);
                            }
                        } else if (
                                downloads_obj.has("rules")
                                        &&
                                        downloads_obj.getAsJsonArray("rules").get(0).getAsJsonObject().has("os")
                        ) {
                            String os_name = downloads_obj.getAsJsonArray("rules")
                                    .get(0).getAsJsonObject()
                                    .getAsJsonObject("os")
                                    .get("name").getAsString();
                            JsonObject artifact = downloads_obj.getAsJsonObject("artifact");
                            if (System.getProperty("os.name").contains(os_name)) {
                                relativePath = artifact.get("path").getAsString();
                                url = artifact.get("url").getAsString();
                                String absolutePath = libDirPath + "/" + relativePath;
                                System.out.println("[INFO]Vanilla Downloader: Downloading native:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                                try {
                                    FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                                    var nativeDir = System.getProperty("oml.gameDir")
                                            + File.separator
                                            + "versions"
                                            + File.separator
                                            + verName
                                            + File.separator
                                            + "natives"
                                            + File.separator;
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
                        } else {
                            relativePath = downloads_obj.getAsJsonObject("artifact").get("path").getAsString();
                            String absolutePath = libDirPath + "/" + relativePath;
                            url = downloads_obj.getAsJsonObject("artifact").get("url").getAsString();
                            System.out.println("[INFO]Vanilla Downloader: Downloading libraries:" + libraries.get(i).getAsJsonObject().get("name").getAsString());
                            try {
                                FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                count[0]++;
            }).start();
        }

        new Thread(() -> {
            while (true) {
                new Thread(() -> {
                    if (count[0] == threadTimes && assetsStatus) {
                        fireWorkspaceStarted("Download Finish");
                    }
                }).start();
            }
        }).start();


    }

    private void nativeDownload(String libDirPath, String relativePath, JsonArray libraries, int i, String url) {
        String absolutePath = libDirPath + "/" + relativePath;
        System.out.println(
                "[INFO]Vanilla Downloader: Downloading native:"
                        + libraries.get(i).getAsJsonObject().get("name").getAsString()

        );
        try {
            FileUtils.writeByteArrayToFile(new File(absolutePath), IOUtils.toByteArray(new URL(url)));
            var nativeDir = System.getProperty("oml.gameDir")
                    + File.separator
                    + "versions"
                    + File.separator
                    + verName
                    + File.separator
                    + "natives"
                    + File.separator;
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

    private void PrivateLog4jConfigBuild(JsonObject versionJson) throws IOException {
        JsonObject client = versionJson.getAsJsonObject("logging").getAsJsonObject("client");
        String url = client.getAsJsonObject("file").get("url").getAsString();
        String id = client.getAsJsonObject("file").get("id").getAsString();
        File config = new File(gameDownloadDir + File.separator + "log4j" + File.separator + id);
        FileUtils.writeByteArrayToFile(config, IOUtils.toByteArray(new URL(url)));
    }
}
