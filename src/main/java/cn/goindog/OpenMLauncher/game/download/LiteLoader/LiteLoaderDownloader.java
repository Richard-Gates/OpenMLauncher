package cn.goindog.OpenMLauncher.game.download.LiteLoader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.net.HttpURLConnection;

public class LiteLoaderDownloader {
    private final String versionsUrl = "https://bmclapi2.bangbang93.com/maven/com/mumfrey/liteloader/versions.json";
    public JsonObject getLiteLoaderVersion() throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(versionsUrl);

        int code = client.executeMethod(method);
        if (code == HttpURLConnection.HTTP_OK) {
            JsonObject response = new Gson().fromJson(
                    method.getResponseBodyAsString(),
                    JsonObject.class
            );
            return response.getAsJsonObject("versions");
        }
        return null;
    }

    public void build(LiteLoaderDownloadProfile profile) throws IOException {
        if (getLiteLoaderVersion().has(profile.getVersion())) {

        }
    }
}
