package cn.goindog.OpenMLauncher.game.download.LiteLoader;

import cn.goindog.OpenMLauncher.exceptions.GameExceptions.UnknownGameVersionException;
import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaDownloader;
import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaInstallProfile;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

public class LiteLoaderDownloader {
    private final String versionsUrl = "https://bmclapi2.bangbang93.com/maven/com/mumfrey/liteloader/versions.json";

    public JsonObject getLiteLoaderVersion() throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(versionsUrl);

        client.executeMethod(method);

        JsonObject response = new Gson().fromJson(
                method.getResponseBodyAsString(),
                JsonObject.class
        );
        return response.getAsJsonObject("versions");
    }

    public void build(LiteLoaderDownloadProfile profile) throws IOException, UnknownGameVersionException {
        if (!getLiteLoaderVersion().has(profile.getVanillaVersion())) {
            throw new UnknownGameVersionException("Unknown LiteLoader Version:" + profile.getVanillaVersion());
        }

        new VanillaDownloader().build(
                new VanillaInstallProfile()
                        .setVersion(profile.getVanillaVersion())
                        .setVersionName(profile.getVanillaVersion()  + "-LiteLoader")
        );


    }

    private void PrivateLiteLoaderDownloads(LiteLoaderDownloadProfile profile) {
        
    }
}
