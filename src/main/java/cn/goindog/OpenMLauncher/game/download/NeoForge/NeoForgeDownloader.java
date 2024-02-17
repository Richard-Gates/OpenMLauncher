package cn.goindog.OpenMLauncher.game.download.NeoForge;

import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaDownloader;
import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaInstallProfile;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NeoForgeDownloader {
    private File installer;
    public void build(NeoForgeDownloadProfile profile) throws IOException {
        Thread vanillaDownload = new Thread(() -> {
            try {
                new VanillaDownloader().build(
                        new VanillaInstallProfile()
                                .setVersion(profile.getVanillaVersion())
                                .setVersionName(
                                        profile.getVanillaVersion()
                                                + "-NeoForge-"
                                                + profile.getNeoforgeVersion()
                                )
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        vanillaDownload.start();

        String downloadUrl = "https://maven.neoforged.net/releases/net/neoforged/neoforge/" + profile.getNeoforgeVersion() + "/neoforge-" + profile.getNeoforgeVersion() + "-installer.jar";
        FileUtils.writeByteArrayToFile(
                new File(
                        System.getProperty("oml.gameDir")
                                + File.separator
                                + "versions"
                                + File.separator
                                + profile.getVanillaVersion()
                                + "-NeoForge-"
                                + profile.getNeoforgeVersion()
                                + File.separator
                                + "forge-installer.jar"
                ),
                IOUtils.toByteArray(
                        new URL(downloadUrl)
                )
        );
        this.installer = new File(
                System.getProperty("oml.gameDir")
                        + File.separator
                        + "versions"
                        + File.separator
                        + profile.getVanillaVersion()
                        + "-NeoForge-"
                        + profile.getNeoforgeVersion()
                        + File.separator
                        + "forge-installer.jar"
        );

        FileUtils.writeByteArrayToFile(
                new File(
                        this.installer.getAbsolutePath().replace("forge-installer.jar", "forge-installer-tool.jar")
                ),
                IOUtils.toByteArray(
                        new URL("https://github.com/Kamesuta/ForgeInstallerCLI/releases/download/1.0.1/ForgeInstallerCLI-1.0.1.jar")
                )
        );
        PrivateInstall(
                new File(
                        this.installer.getAbsolutePath().replace("forge-installer.jar", "forge-installer-tool.jar")
                ),
                profile
        );
    }

    private void PrivateInstall(File cli_jar,NeoForgeDownloadProfile profile) {


        StringBuilder installCommand = new StringBuilder();
        installCommand.append("java -jar \"")
                .append(cli_jar.getAbsolutePath())
                .append("\" --installer \"")
                .append(this.installer.getAbsolutePath())
                .append("\" --target \"")
                .append(System.getProperty("oml.gameDir"))
                .append("\" --progress");

        try {
            Process process = Runtime.getRuntime().exec(installCommand.toString());
            String input = consumeInputStream(process, "medium");
            String errStr = consumeInputStream(process, "error");
            process.waitFor();

            FileUtils.copyFile(
                    new File(
                            System.getProperty("oml.gameDir")
                            + File.separator
                            + "versions"
                            + File.separator
                            + "neoforge-"
                            + profile.getNeoforgeVersion()
                            + File.separator
                            + "neoforge-"
                            + profile.getNeoforgeVersion()
                            + ".json"
                    ),
                    new File(
                            System.getProperty("oml.gameDir")
                                    + File.separator
                                    + "versions"
                                    + File.separator
                                    + profile.getVanillaVersion()
                                    + "-NeoForge-"
                                    + profile.getNeoforgeVersion()
                                    + File.separator
                                    + profile.getVanillaVersion()
                                    + "-NeoForge-"
                                    + profile.getNeoforgeVersion()
                                    + ".json"
                    )
            );

            FileUtils.deleteDirectory(
                    new File(
                            System.getProperty("oml.gameDir")
                                    + File.separator
                                    + "versions"
                                    + File.separator
                                    + "neoforge-"
                                    + profile.getNeoforgeVersion()
                                    + File.separator
                    )
            );

            MergeJson();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    private void MergeJson() throws IOException {
        String separator = File.separator;
        String[] forgeJsonPathList = getInstaller().getAbsolutePath().split("\\" + separator);
        String forgeVersionPath = getInstaller().getAbsolutePath().replace("forge-installer.jar", forgeJsonPathList[forgeJsonPathList.length - 2] + ".json");
        JsonObject forgeVersionJson = new Gson().fromJson(
                FileUtils.readFileToString(
                        new File(
                                forgeVersionPath
                        ),
                        StandardCharsets.UTF_8
                ),
                JsonObject.class
        );

        String version = forgeVersionJson.get("inheritsFrom").getAsString();

        JsonObject vanillaVersionJson = new Gson().fromJson(
                FileUtils.readFileToString(
                        new File(
                                getInstaller().getAbsolutePath().replace("forge-installer.jar", version + ".json")
                        ),
                        StandardCharsets.UTF_8
                ),
                JsonObject.class
        );

        JsonObject mergeJson = versionJsonMerge(forgeVersionJson, vanillaVersionJson);

        FileUtils.writeStringToFile(
                new File(
                        forgeVersionPath
                ),
                mergeJson.toString(),
                StandardCharsets.UTF_8
        );
    }

    private File getInstaller() {
        return installer;
    }

    private String consumeInputStream(Process process,String type) throws IOException {
        InputStream is;
        if (type.contains("medium")) {
            is = process.getInputStream();
        } else {
            is = process.getErrorStream();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null) {
            System.out.println(s);
            sb.append(s);
        }
        return sb.toString();
    }


    private JsonObject versionJsonMerge(JsonObject forgeVersionJson, JsonObject vanillaVersionJson) {
        for (String key : vanillaVersionJson.keySet()) {
            if (!forgeVersionJson.has(key)) {
                forgeVersionJson.add(key, vanillaVersionJson.get(key));
            }
        }
        for (JsonElement element : vanillaVersionJson.getAsJsonArray("libraries")) {
            JsonArray libs = forgeVersionJson.getAsJsonArray("libraries");
        }
        for (JsonElement element : vanillaVersionJson.getAsJsonObject("arguments").getAsJsonArray("game")) {
            forgeVersionJson.getAsJsonObject("arguments").getAsJsonArray("game").add(element);
        }
        if ( vanillaVersionJson.getAsJsonObject("arguments").has("jvm")) {
            for (JsonElement element : vanillaVersionJson.getAsJsonObject("arguments").getAsJsonArray("jvm")) {
                forgeVersionJson.getAsJsonObject("arguments").getAsJsonArray("jvm").add(element);
            }
        }
        forgeVersionJson.add("clientVersion", forgeVersionJson.get("inheritsFrom"));
        forgeVersionJson.remove("inheritsFrom");
        return forgeVersionJson;
    }
}
