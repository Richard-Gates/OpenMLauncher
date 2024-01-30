package cn.goindog.OpenMLauncher.account.Offline;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class OfflineController {
    public void build(String user_name) {
        UUID uuid = UUID.nameUUIDFromBytes(user_name.getBytes());
        try {
            File user_config_file = new File(
                    System.getProperty("user.dir") + "/.openmlauncher/user.json"
            );
            JsonObject user_config;
            if (user_config_file.exists()) {
                user_config = new Gson().fromJson(
                        FileUtils.readFileToString(
                                user_config_file
                                ,
                                StandardCharsets.UTF_8
                        ),
                        JsonObject.class
                );
            } else {
                user_config = new JsonObject();
                user_config.add("users", new JsonArray());
            }

            JsonObject user = new JsonObject();
            user.addProperty("type", "offline");

            JsonObject profile = new JsonObject();
            profile.addProperty("name", user_name);
            profile.addProperty("id", uuid.toString());

            user.add("profile", profile);

            if (user_config.has("users")) {
                if (!user_config.get("users").getAsJsonArray().isEmpty()) {
                    for (int i = 0; i < user_config.getAsJsonArray("users").size(); i++) {
                        JsonArray arr = user_config.getAsJsonArray("users");
                        String type = arr.get(i).getAsJsonObject().get("type").getAsString();
                        if (Objects.equals(type, "offline")) {
                            String name = arr.get(i).getAsJsonObject().getAsJsonObject("profile").get("name").getAsString();
                            if (i != user_config.size()) {
                                if (Objects.equals(name, user_name)) {
                                    break;
                                }
                            } else {
                                if (Objects.equals(name, user_name)) {
                                    break;
                                } else {
                                    user_config.getAsJsonArray("users").add(user);
                                }
                            }
                        }
                    }
                } else {
                    user_config.getAsJsonArray("users").add(user);
                }
            }

            FileUtils.writeStringToFile(
                    user_config_file,
                    user_config.toString(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
