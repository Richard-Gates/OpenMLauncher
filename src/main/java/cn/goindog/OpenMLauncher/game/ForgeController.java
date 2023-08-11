package cn.goindog.OpenMLauncher.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.Thread.State.TERMINATED;

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

    public void Get(ForgeInstallerConfig config) throws IOException {
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
        });
        Thread install_tool_download = new Thread(() -> {
            try {
                PrivateInstallToolDownload(conf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        installer_download.start();
        install_tool_download.start();
        while (true) {
            if (install_tool_download.getState().equals(TERMINATED)) {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("cmd.exe java -cp \"forge-installer-headless.jar;forge-installer.jar\" me.xfl03.HeadlessInstaller -installClient " + System.getProperty("oml.gameDir"), null, new File(System.getProperty("oml.gameDir") + "/versions/" + conf.getGameVer() + "-forge-" + conf.getForgeVer()));
            }
        }
    }

    private static void PrivateInstallerDownload(ForgeInstallerConfig conf) {
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

    private static void PrivateInstallToolDownload(ForgeInstallerConfig conf) throws IOException {
        String installJarPath = System.getProperty("oml.gameDir") + "/versions/" + conf.getGameVer() + "-forge-" + conf.getForgeVer() + "/forge-install-tool.jar";
        System.out.println("[INFO]Downloading InstallTools");
        FileUtils.writeByteArrayToFile(new File(installJarPath), IOUtils.toByteArray(new URL("https://github.com/xfl03/ForgeInstallerHeadless/releases/download/1.0.1/forge-installer-headless-1.0.1.jar")));

    }

    public static void main(String[] args) throws IOException {
        ForgeInstallerConfig config = new ForgeInstallerConfig();
        config.setForgeVer("36.2.39");
        config.setGameVer("1.16.5");
        new ForgeController().Get(config);
    }
}
