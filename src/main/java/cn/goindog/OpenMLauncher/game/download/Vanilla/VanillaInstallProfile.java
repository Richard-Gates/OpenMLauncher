package cn.goindog.OpenMLauncher.game.download.Vanilla;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.net.HttpURLConnection;

public class VanillaInstallProfile {
    private String version = "";
    private String versionName = version;
    /**
     * 最新正式版
     */
    public static String LATEST_RELEASES_VERSION = "";
    /**
     * 最新快照版
     */
    public static String LATEST_SNAPSHOT_VERSION = "";

    static {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod("https://piston-meta.mojang.com/mc/game/version_manifest.json");

        try {
            int code = client.executeMethod(method);
            switch (code) {
                case HttpURLConnection.HTTP_OK -> {
                    JsonObject manifest = new Gson().fromJson(
                            method.getResponseBodyAsString(),
                            JsonObject.class
                    );
                    LATEST_RELEASES_VERSION = manifest.getAsJsonObject("latest")
                            .get("release").getAsString();
                    LATEST_SNAPSHOT_VERSION = manifest.getAsJsonObject("latest")
                            .get("snapshot").getAsString();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个安装实例
     */
    public VanillaInstallProfile() {

    }

    /**
     * 创建一个配置实例
     * @param version 游戏版本
     */
    public VanillaInstallProfile(String version) {
        this.version = version;
    }

    /**
     * 获取游戏版本
     * @return 游戏版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置游戏版本
     * @param version 游戏版本
     * @return 设置游戏版本后的安装配置实例
     */
    public VanillaInstallProfile setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * 获取游戏版本
     * @return 游戏版本
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * 设置游戏版本名称
     * @param versionName 游戏版本名称
     * @return 设置版本名称后的安装配置实例
     */
    public VanillaInstallProfile setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }
}
