package cn.goindog.OpenMLauncher.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class VanillaController {
    private static final String manifest_url = "https://piston-meta.mojang.com/mc/game/version_manifest.json";
    public String gameDir = System.getProperty("user.dir") + "/.minecraft";

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

    public void Get(String versionName) throws IOException {
        System.out.println("[INFO]Reading version_manifest.json file.");
        JsonArray version_manifest_arr = getAllGameVersion();
        for (int vm_index = 0; vm_index < version_manifest_arr.size(); vm_index++) {
            if (Objects.equals(version_manifest_arr.get(vm_index).getAsJsonObject().get("id").getAsString(), versionName)) {
                String gameJsonUrl = version_manifest_arr.get(vm_index).getAsJsonObject().get("url").getAsString();
                PrivateCenterDownload(new URL(gameJsonUrl), versionName);
            }
        }
    }

    private static void PrivateCenterDownload(URL versionJson, String verName) throws IOException {
        JsonObject versionJsonObj = new Gson().fromJson(IOUtils.toString(versionJson, StandardCharsets.UTF_8), JsonObject.class);
        System.out.println("[INFO]Downloading client.jar file.");
        var clientJarUrl = versionJsonObj.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString();
        var versionDir = System.getProperty("oml.gameDir") + "/versions/" + verName;
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
        Thread writeGameJsonThread = new Thread(() -> {
            try {
                FileUtils.writeByteArrayToFile(new File(versionDir + "/" + verName + ".json"), IOUtils.toByteArray(versionJson));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        asset_download.start();
        lib_download.start();
        writeGameJsonThread.start();
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
