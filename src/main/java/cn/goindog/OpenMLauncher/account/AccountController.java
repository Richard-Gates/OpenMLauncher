package cn.goindog.OpenMLauncher.account;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AccountController {
    static JsonObject user_config;

    static {
        try {
            user_config = new Gson().fromJson(
                    FileUtils.readFileToString(
                            new File(
                                    System.getProperty("user.dir")
                                    + "/.openmlauncher/user.json"
                            ),
                            StandardCharsets.UTF_8
                    ),
                    JsonObject.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 切换选中用户
     * @param userName 用户名
     */
    public static void Selector(String userName) {
        JsonArray users = user_config.getAsJsonArray("users");
        for(
                int i = 0;
                i < users.size();
                i++
        ) {
            String nowUserName = users
                    .get(i).getAsJsonObject()
                    .getAsJsonObject("profile")
                    .get("name").getAsString();
            if (nowUserName.equals(userName)) {
                user_config.remove("selector");
                user_config.addProperty("selector", i);
                break;
            }
        }
    }

    /**
     * 删除用户
     * @param userName 用户名
     */

    public static void Delete(String userName) {
        JsonArray users = user_config.getAsJsonArray("users");
        for(
                int i = 0;
                i < users.size();
                i++
        ) {
            String nowUserName = users
                    .get(i).getAsJsonObject()
                    .getAsJsonObject("profile")
                    .get("name").getAsString();
            if (nowUserName.equals(userName)) {
                users.remove(i);
                if (
                        user_config.get("selector").getAsInt() == i
                ) {
                    user_config.remove("selector");
                    user_config.addProperty("selector", i - 1);
                }
                break;
            }
        }
    }
}
