package cn.goindog.OpenMLauncher.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.File;
import java.util.Objects;

public class GameController {
    public JsonArray getInstalledVersions(File versionDir) {
        JsonArray installedVersions = new Gson().fromJson("[]", JsonArray.class);
        for (File child : Objects.requireNonNull(versionDir.listFiles())) {
            installedVersions.add(child.getName());
        }
        return installedVersions;
    }

    public void uninstallVersion(File versionsDir,String versionName) {
        JsonArray installedVersions = getInstalledVersions(versionsDir);
        for (JsonElement installedVerName:installedVersions) {
            if (installedVerName.getAsString().equals(versionName)) {
                File versionDir = new File(versionsDir.getAbsolutePath() + File.separator + versionName);
                versionDir.deleteOnExit();
            }
        }
    }
}
