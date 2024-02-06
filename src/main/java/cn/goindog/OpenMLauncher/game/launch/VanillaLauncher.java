package cn.goindog.OpenMLauncher.game.launch;

import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaDownloader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class VanillaLauncher {
    private String gamePath;

    public void setGamePath(String gamePath) {
        this.gamePath = gamePath;
    }

    public String getGamePath() {
        return gamePath;
    }

    public void build(String gameName) throws IOException, InterruptedException {
        JsonObject versionJson = new Gson().fromJson(FileUtils.readFileToString(new File(gamePath + File.separator + gameName + ".json"), StandardCharsets.UTF_8), JsonObject.class);
        launch(versionJson, gameName);
    }

    private void launch(JsonObject versionJson, String gameName) throws IOException, InterruptedException {
        JsonObject userConfig = this.getUserConfig();
        String type = userConfig.get("type").getAsString();
        JsonObject profile = userConfig.getAsJsonObject("profile");
        String userName = profile.get("name").getAsString();
        String mcToken = userConfig.get("minecraft_token").getAsString();
        String uuid = profile.get("id").getAsString();
        String gameDir = System.getProperty("oml.gameDir") + File.separator + "versions" + File.separator + gameName;
        String assetsDir = gameDir + File.separator + "assets";
        String assetIndex = versionJson.getAsJsonObject("assetIndex").get("id").getAsString();
        String mainClass = versionJson.get("mainClass").getAsString();
        JsonArray libraries = versionJson.getAsJsonArray("libraries");
        JsonObject argument;
        if (versionJson.has("arguments")) {
            argument = versionJson.get("arguments").getAsJsonObject();
        } else {
            argument = new JsonObject();
            StringBuilder arguments = new StringBuilder();
            arguments.append("[");

            String[] argumentStr = versionJson.get("minecraftArguments").getAsString().split(" ");
            System.out.println(Arrays.toString(argumentStr));
            for (int i = 0; i < argumentStr.length; i++) {
                arguments.append("\"").append(argumentStr[i]).append("\"");
                if (!(i == argumentStr.length - 1)) {
                    arguments.append(",");
                }
            }
            arguments.append("]");
            System.out.println(arguments);
            JsonArray game_argument = new Gson().fromJson(String.valueOf(arguments), JsonArray.class);
            argument.add("game", game_argument);
        }

        String launchCommand = getLaunchCommand(uuid, mcToken, userName, type, assetIndex, assetsDir, gameDir, gameName, mainClass, libraries, argument);

        String assetsIndexUrl = versionJson.get("assetIndex").getAsJsonObject().get("url").getAsString();
        System.out.println("[INFO]GameStarter Thread: Starting Game: (launchCommand)" + launchCommand);

        Thread thread = new Thread(() -> {
            VanillaDownloader downloader = new VanillaDownloader();
            try {
                downloader.reDownloadFiles(new URL(assetsIndexUrl), assetsDir, assetIndex, libraries);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        try {
            Process process = Runtime.getRuntime().exec(launchCommand);
            String inStr = consumeInputStream(process.getInputStream());
            String errStr = consumeInputStream(process.getErrorStream());
            int proc = process.waitFor();
            if (proc == 0) {
                System.out.println("执行成功");
            } else {
                System.out.println("执行失败" + errStr);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private JsonObject getUserConfig() throws IOException {
        File userConfig = new File(System.getProperty("user.dir") + File.separator + ".openmlauncher" + File.separator + "user.json");
        JsonObject userConfigObj = new Gson().fromJson(FileUtils.readFileToString(userConfig, StandardCharsets.UTF_8), JsonObject.class);
        int selectorIndex = userConfigObj.get("selector").getAsInt();
        return userConfigObj.getAsJsonArray("users").get(selectorIndex).getAsJsonObject();
    }

    private String getLaunchCommand(String uuid, String token, String userName, String userType, String assetIndex, String assetDir, String gameDir, String gameVersion, String mainClass, JsonArray libraries, JsonObject arguments) {
        StringBuilder libs = new StringBuilder();
        String separator;
        if (System.getProperty("os.name").contains("Windows")) {
            separator = ";";
        } else {
            separator = ":";
        }
        for (JsonElement element : libraries) {
            if (element.getAsJsonObject().get("downloads").getAsJsonObject().has("artifact")) {
                String path = element.getAsJsonObject().getAsJsonObject("downloads").getAsJsonObject("artifact").get("path").getAsString();
                if (System.getProperty("os.name").contains(element.getAsJsonObject().get("name").getAsString())) {
                    if (element.getAsJsonObject().getAsJsonArray("rules").get(0).getAsJsonObject().has("os") && System.getProperty("os.name").contains(element.getAsJsonObject().getAsJsonArray("rules").get(0).getAsJsonObject().getAsJsonObject("os").get("name").getAsString()))
                    {
                        libs.append(assetDir.replace("assets", "libraries/" + path)).append(separator);
                    }
                } else {
                    libs.append(assetDir.replace("assets", "libraries/" + path)).append(separator);
                }
            }
        } libs.append(";").append(gameDir).append(File.separator).append(gameVersion).append(".jar");

        StringBuilder JVMBuilder = new StringBuilder();
        if (arguments.has("jvm")) {
            for (JsonElement element : arguments.getAsJsonArray("jvm")) {
                if (element.isJsonObject()) {
                    JsonObject rule = element.getAsJsonObject().getAsJsonArray("rules").get(0).getAsJsonObject();
                    if (rule.has("os")) {
                        if (rule.getAsJsonObject("os").has("name")) {
                            String os = rule.getAsJsonObject("os").get("name").getAsString();
                            if (System.getProperty("os.name").toLowerCase().contains(os)) {
                                if (!element.getAsJsonObject().get("value").isJsonArray()) {
                                    JVMBuilder.append(" \"").append(element.getAsJsonObject().get("value").getAsString()).append("\"");
                                } else {
                                    for (JsonElement e : element.getAsJsonObject().get("value").getAsJsonArray()) {
                                        JVMBuilder.append(" \"").append(e.getAsString()).append("\"");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (element.getAsString().contains("${natives_directory}")) {
                        JVMBuilder.append(" ").append(element.getAsString().replace("${natives_directory}", gameDir + File.separator + "natives"));
                    } else if (element.getAsString().contains("${launcher_name}")) {
                        JVMBuilder.append(" ").append(element.getAsString().replace("${launcher_name}", "OpenMLauncher"));
                    } else if (element.getAsString().contains("${launcher_version}")) {
                        JVMBuilder.append(" ").append(element.getAsString().replace("${launcher_version}", "20"));
                    } else if (element.getAsString().contains("${version_name}")) {
                        JVMBuilder.append(" \"").append(element.getAsString().replace("${version_name}", gameVersion)).append("\"");
                    } else if (element.getAsString().contains("${classpath_separator}") && element.getAsString().contains("${library_directory}")) {
                        JVMBuilder.append(" \"").append(element.getAsString().replace("${library_directory}", gameDir + "/libraries/").replace("${classpath_separator}", separator)).append("\"");
                    } else if (element.getAsString().contains("${library_directory}")) {
                        JVMBuilder.append(" \"").append(element.getAsString().replace("${library_directory}", gameDir + "/libraries/")).append("\"");
                    } else if (element.getAsString().contains("${classpath}")) {
                        JVMBuilder.append(" ");
                    } else if (element.getAsString().contains("-cp")) {
                        JVMBuilder.append(" ");
                    } else {
                        JVMBuilder.append(" ").append(element.getAsString());
                    }
                }
            }
        }

        JVMBuilder.append(" -cp \"").append(libs).append("\" -Djava.library.path=\"").append(gameDir).append(File.separator).append("natives\"");

        StringBuilder gameArguments = new StringBuilder();
        for (JsonElement element : arguments.getAsJsonArray("game")) {
            if (!element.isJsonObject()) {
                if (element.getAsString().startsWith("${")) {
                    if (element.getAsString().contains("auth_uuid")) {
                        gameArguments.append(uuid);
                    } else if (element.getAsString().contains("auth_access_token")) {
                        gameArguments.append(token);
                    } else if (element.getAsString().contains("auth_player_name")) {
                        gameArguments.append(userName);
                    } else if (element.getAsString().contains("user_type")) {
                        gameArguments.append(userType);
                    } else if (element.getAsString().contains("version_type")) {
                        gameArguments.append("OpenMLauncher");
                    } else if (element.getAsString().contains("assets_index_name")) {
                        gameArguments.append(assetIndex);
                    } else if (element.getAsString().contains("assets_root")) {
                        gameArguments.append(assetDir);
                    } else if (element.getAsString().contains("game_directory")) {
                        gameArguments.append(gameDir);
                    } else if (element.getAsString().contains("version_name")) {
                        gameArguments.append(gameVersion);
                    } else {
                        gameArguments.append(" ").append(element.getAsString()).append(" ");
                    }
                } else {
                    gameArguments.append(" ").append(element.getAsString()).append(" ");
                }
            }
        }
        return "java " + JVMBuilder + " " + mainClass + " " + gameArguments;
    }

    public static String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null) {
            System.out.println(s);
            sb.append(s);
        }
        return sb.toString();
    }
}
