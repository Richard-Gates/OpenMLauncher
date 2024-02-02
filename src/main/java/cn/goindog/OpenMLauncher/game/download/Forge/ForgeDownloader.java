package cn.goindog.OpenMLauncher.game.download.Forge;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

    public void build(ForgeInstallerProfile profile) throws IOException {
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

    private static void PrivateCenterDownload(ForgeInstallerProfile conf) {
        Thread installer_download = new Thread(() -> {
            PrivateInstallerDownload(conf);
            String path = System.getProperty("oml.gameDir") + "/versions/" + conf.getGameVer() + "-forge-" + conf.getForgeVer() + "/";
            File installer = new File(path +  "/forge-installer.jar");
            try {
                PrivateInstall(installer);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        installer_download.start();
    }

    private static void PrivateInstallerDownload(ForgeInstallerProfile conf) {
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

    private static void PrivateInstall(File Installer) throws IOException, InterruptedException {
        new ForgeOldInstaller().setInstaller(Installer).build();
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

        public void build() {

        }
    }

    static class ForgeNewInstaller {
        private File installer;

        public ForgeNewInstaller() {

        }

        public ForgeNewInstaller(File installer) {
            this.installer = installer;
        }

        public ForgeNewInstaller setInstaller(File installer) {
            this.installer = installer;
            return this;
        }

        public File getInstaller() {
            return this.installer;
        }
    }
}
