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
    public static String LATEST_RELEASES_VERSION = "";
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

    public VanillaInstallProfile() {

    }

    public VanillaInstallProfile(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public VanillaInstallProfile setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public VanillaInstallProfile setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }
}
