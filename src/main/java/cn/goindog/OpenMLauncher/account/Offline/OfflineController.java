package cn.goindog.OpenMLauncher.account.Offline;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class OfflineController {
    public void build(String user_name) {
        UUID uuid = UUID.nameUUIDFromBytes(user_name.getBytes());
        String token = UUID.randomUUID().toString().replace("-", "");
        try {
            File user_config_file = new File(System.getProperty("user.dir") + "/.openmlauncher/user.json");
            JsonObject user_config;
            if (user_config_file.exists()) {
                user_config = new Gson().fromJson(FileUtils.readFileToString(user_config_file, StandardCharsets.UTF_8), JsonObject.class);
            } else {
                user_config = new JsonObject();
                user_config.add("users", new JsonArray());
            }

            JsonObject user = new JsonObject();
            user.addProperty("type", "offline");

            JsonObject profile = new JsonObject();
            profile.addProperty("name", user_name);
            profile.addProperty("id", uuid.toString());
            profile.add("profileActions", new JsonObject());
            profile.addProperty("minecraft_token", token);

            JsonArray skins = new JsonArray();
            JsonObject skin = new JsonObject();
            skin.addProperty("id", UUID.nameUUIDFromBytes((user_name + "_skin").getBytes()).toString());
            skin.addProperty("state", "ACTIVE");
            int userNameHash = user_name.hashCode();
            int verifyNumber = userNameHash++ % new Random().nextInt(userNameHash - (2 * userNameHash));
            if (userNameHash * 10 > verifyNumber * new Random().nextInt(9) + 1) {
                skin.addProperty("url", "https://zh.minecraft.wiki/images/Steve_%28classic_texture%29_JE6.png");
            } else {
                skin.addProperty("url", "https://zh.minecraft.wiki/images/Alex_%28classic_texture%29_JE2.png");
            }
            skin.addProperty("variant", "CLASSIC");

            skins.add(skin);

            profile.add("skins", skins);
            user.add("profile", profile);

            JsonArray arr = user_config.getAsJsonArray("users");

            if (!arr.getAsJsonArray().isEmpty()) {
                for (int i = 0; i < arr.size(); i++) {
                    if (i != arr.size() - 1) {
                        if (
                                arr.get(i).getAsJsonObject().getAsJsonObject("profile").get("name").getAsString().equals(user_name)
                                        &&
                                        arr.get(i).getAsJsonObject().get("type").getAsString().equals("offline")
                        ) {
                            break;
                        }
                    } else {
                        if (!arr.get(i).getAsJsonObject().getAsJsonObject("profile").get("name").getAsString().equals(user_name)) {
                            user_config.getAsJsonArray("users").add(user);
                        }
                    }
                }
            } else {
                user_config.getAsJsonArray("users").add(user);
            }

            FileUtils.writeStringToFile(user_config_file, user_config.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
