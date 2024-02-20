package cn.goindog.OpenMLauncher.account;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AccountController {
    private static final String change_skin_url = "https://api.mojang.com/user/profile/<uuid>/skin";
    private static  final  String get_skin_url = "https://sessionserver.mojang.com/session/minecraft/profile/<uuid>";
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

    /**
     * 获取当前选中用户信息
     * @return 当前选中用户JsonObject实例
     * @throws IOException 对用户配置文件读取需要IO操作
     */
    public static JsonObject getSelectorUser() throws IOException {
        JsonObject userJson = new Gson().fromJson(
                FileUtils.readFileToString(
                        new File(
                                System.getProperty("user.dir")
                                + File.separator
                                + ".openmlauncher"
                                + File.separator
                                + "user.json"
                        ),
                        StandardCharsets.UTF_8
                ),
                JsonObject.class
        );
        int selectorIndex = userJson.get("selector").getAsInt();
        JsonArray users = userJson.getAsJsonArray("users");
        return users.get(selectorIndex).getAsJsonObject();
    }

    /**
     * 更改当前选中用户的皮肤
     * @param skin 皮肤文件
     * @throws IOException 获取用户配置需要对本地文件执行IO操作
     */
    public static void PutSkin(File skin, SkinType type) throws IOException {
        JsonObject user_config = getSelectorUser();

        String access_token = user_config.get("minecraft_token").getAsString();
        String uuid = user_config.getAsJsonObject("profile").get("id").getAsString();

        byte[] image_data = IOUtils.toByteArray(new FileInputStream(skin));

        HttpClient client = new HttpClient();
        PutMethod method = new PutMethod(change_skin_url.replace("<uuid>", uuid));
        method.addRequestHeader("Authorization", "Bearer " + access_token);

        String skinType;
        if (type.name().equals("SLIM")) {
            skinType = "slim";
        } else {
            skinType = "";
        }

        HttpMethodParams params = new HttpMethodParams();
        params.setParameter("file", new String(image_data));
        params.setParameter("model", skinType);

        method.setParams(params);

        client.executeMethod(method);
    }

    /**
     * 更换玩家皮肤
     * @param skin 皮肤文件
     * @param type 皮肤模型类型
     * @throws IOException 对用户配置读取需要进行IO操作
     */
    public static void ChangeSkin(File skin, SkinType type) throws IOException {
        JsonObject user_config = getSelectorUser();

        String token = user_config.get("minecraft_token").getAsString();
        String uuid = user_config.getAsJsonObject("profile").get("id").getAsString();

        String model;
        if (type == SkinType.SLIM) {
            model = "slim";
        } else {
            model = "";
        }

        byte[] image_data = IOUtils.toByteArray(
                new FileInputStream(skin)
        );

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(change_skin_url.replace("<uuid>", uuid) + "?model=" + model + "&file=" + new String(image_data));
        method.addRequestHeader("Authorization", "Bearer " + token);

        client.executeMethod(method);
    }

    /**
     * 重置玩家皮肤
     * @throws IOException 对用户配置文件的读取需要进行IO操作
     */
    public static void ResetSkin() throws IOException {
        JsonObject user_config = getSelectorUser();

        String token = user_config.get("minecraft_token").getAsString();
        String uuid = user_config.getAsJsonObject("profile").get("id").getAsString();

        HttpClient client = new HttpClient();
        DeleteMethod method = new DeleteMethod(change_skin_url.replace("<uuid>", uuid));

        method.addRequestHeader("Authorization", "Bearer " + token);

        client.executeMethod(method);
    }

    /**
     * 获取用户皮肤配置
     * @return 返回用户皮肤配置Json对象
     * @throws IOException 对用户配置文件读取需要IO操作
     * @throws DecoderException 对base64加密的用户皮肤配置解码
     */
    public static JsonObject getSkin() throws IOException, DecoderException {
        JsonObject user_config = getSelectorUser();

        String uuid = user_config.getAsJsonObject("profile").get("id").getAsString();

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(get_skin_url.replace("<uuid>", uuid));

        client.executeMethod(method);

        String response = method.getResponseBodyAsString();
        String base64_profile = new Gson().fromJson(
                response,
                JsonObject.class
        ).getAsJsonArray("properties")
                .get(0).getAsJsonObject()
                .get("value").getAsString();

        Base64 base64 = new Base64();

        String encoded = (String) base64.decode(base64_profile);

        return new Gson().fromJson(
                encoded,
                JsonObject.class
        );
    }
}
