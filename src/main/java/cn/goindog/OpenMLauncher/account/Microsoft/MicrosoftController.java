package cn.goindog.OpenMLauncher.account.Microsoft;

import cn.goindog.OpenMLauncher.events.OAuthEvents.OAuthFinishEventListener;
import cn.goindog.OpenMLauncher.events.OAuthEvents.OAuthFinishEvent;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

public class MicrosoftController {
    private static final String xbl_url = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String xsts_url = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String Minecraft_Authenticate_Url = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String Check_Url = "https://api.minecraftservices.com/entitlements/mcstore";
    private static final String get_profile_url = "https://api.minecraftservices.com/minecraft/profile";
    private static String refresh_token = "";
    private Collection listeners;

    public void addOAuthFinishListener(OAuthFinishEventListener listener) {
        if (listeners == null) {
            listeners = new HashSet();
        }
        listeners.add(listener);
    }

    public void removeOAuthFinishListener(OAuthFinishEventListener listener) {
        if (listeners == null)
            return;
        listeners.remove(listener);
    }

    protected void fireWorkspaceStarted(String type) {
        if (listeners == null)
            return;
        OAuthFinishEvent event = new OAuthFinishEvent(this, type);
        notifyListeners(event);
    }

    private void notifyListeners(OAuthFinishEvent event) {
        for (Object o : listeners) {
            OAuthFinishEventListener listener = (OAuthFinishEventListener) o;
            listener.OAuthFinishEvent(event);
        }
    }


    public void build(MicrosoftOAuthOptions options) {
        switch (options.getType()) {
            case DEVICE -> {
                System.out.println("[INFO]Microsoft Login Method:Device Code Flow");
                MicrosoftOAuthDeviceFlowMethods methods = new MicrosoftOAuthDeviceFlowMethods();
                methods.addOAuthFinishListener(event -> {
                    JsonObject returnObj = methods.getObject();
                    String access_token = returnObj.get("access_token").getAsString();
                    if (returnObj.has("refresh_token")) {
                        refresh_token = returnObj.get("refresh_token").getAsString();
                    }
                    try {
                        XblAuthenticate(access_token);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                methods.build(options.getScopes());
            }
            case AUTHORIZATION_CODE -> {
                System.out.println("[INFO]Microsoft Login Method:Authorization Code Flow");
                MicrosoftOAuthAuthorizationCodeMethods methods = new MicrosoftOAuthAuthorizationCodeMethods();
                methods.build(options.getMethod());
                methods.addOAuthFinishListener(event -> {
                    JsonObject returnObj = methods.getObject();
                    String access_token = returnObj.get("access_token").getAsString();
                    if (returnObj.has("refresh_token")) {
                        refresh_token = returnObj.get("refresh_token").getAsString();
                    }
                    try {
                        XblAuthenticate(access_token);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    private void XblAuthenticate(String accessToken) throws IOException {
        System.out.println("[INFO]Getting XBox Live Token & UserHash");

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(xbl_url);
        method.addRequestHeader("Content-Type", "application/json");
        method.addRequestHeader("Accept", "application/json");

        RequestEntity entity = new ByteArrayRequestEntity(
                (
                        """
                                {
                                    "Properties": {
                                        "AuthMethod": "RPS",
                                        "SiteName": "user.auth.xboxlive.com",
                                        "RpsTicket": "d=""" + accessToken + """
                                    "},
                                    "RelyingParty": "http://auth.xboxlive.com",
                                    "TokenType": "JWT"
                                }
                                """
                ).getBytes()
        );
        method.setRequestEntity(entity);

        int code = client.executeMethod(method);

        if (code == HttpURLConnection.HTTP_OK) {
            String xbl_body = method.getResponseBodyAsString();

            JsonObject xbl_resp_obj = new Gson().fromJson(xbl_body, JsonObject.class);
            String xbl_token = xbl_resp_obj.get("Token").getAsString();
            System.out.println("[INFO]Get XBox Live Token & UserHash Complete");
            XstsAuthenticate(xbl_token);
        } else {
            System.out.println("Bad Connection:" + code);
        }
    }

    private void XstsAuthenticate(String XblToken) {
        System.out.println("[INFO]Getting XSTS Token & UserHash");
        JsonObject data = new Gson().fromJson("""
                {
                    "Properties": {
                        "SandboxId": "RETAIL",
                        "UserTokens":""" + List.of(XblToken) + """
                    },
                    "RelyingParty": "rp://api.minecraftservices.com/",
                    "TokenType": "JWT"
                }
                """, JsonObject.class);

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(xsts_url);
        method.addRequestHeader("Content-Type", "application/json");
        method.addRequestHeader("Accept", "application/json");

        try {
            RequestEntity entity = new StringRequestEntity(data.toString(), "application/json", "UTF-8");
            method.setRequestEntity(entity);

            int code = client.executeMethod(method);

            if (code == HttpURLConnection.HTTP_OK) {
                String xsts_body = method.getResponseBodyAsString();

                JsonObject xsts_resp_obj = new Gson().fromJson(xsts_body, JsonObject.class);
                String xsts_token = xsts_resp_obj.get("Token").getAsString();
                String xsts_uhs = xsts_resp_obj.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
                System.out.println("[INFO]Get XSTS Token & UserHash Complete");
                MinecraftAuthenticate(xsts_token, xsts_uhs);
            } else {
                System.out.println("Bad Connection:" + code);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void MinecraftAuthenticate(String XSTSToken, String UHS) {
        System.out.println("[INFO]Getting Minecraft Account Token & UUID");

        JsonObject data = new Gson().fromJson("""
                {
                    "identityToken": "XBL3.0 x=""" + UHS + ";" + XSTSToken +
                """
                        "
                        }
                        """, JsonObject.class);

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(Minecraft_Authenticate_Url);

        try {
            RequestEntity entity = new StringRequestEntity(data.toString(), "application/json", "UTF-8");
            method.setRequestEntity(entity);

            int code = client.executeMethod(method);

            if (code == HttpURLConnection.HTTP_OK) {
                String mc_auth_body = method.getResponseBodyAsString();

                JsonObject mc_auth_obj = new Gson().fromJson(mc_auth_body, JsonObject.class);
                String mc_token = mc_auth_obj.get("access_token").getAsString();
                System.out.println("[INFO]Get Minecraft Account Token & UUID Complete");
                CheckHaveMinecraft(mc_token);
            } else {
                System.out.println("Bad Connection:" + code);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void CheckHaveMinecraft(String MinecraftToken) {
        System.out.println("[INFO]Checking Minecraft Possession");

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(Check_Url);
        method.addRequestHeader("Authorization", "Bearer " + MinecraftToken);

        try {
            int code = client.executeMethod(method);

            if (code == HttpURLConnection.HTTP_OK) {
                String body = method.getResponseBodyAsString();
                JsonObject resp_obj = new Gson().fromJson(body, JsonObject.class);
                if (resp_obj.has("items")) {
                    GetProfile(MinecraftToken);
                } else {
                    System.out.println("[WARN]Failed to complete the login!");
                    System.out.println("[ERROR]Sorry, your Microsoft account does not have Minecraft, failed to complete the login!");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void GetProfile(String MinecraftToken) {
        System.out.println("[INFO]Check Minecraft Possession Complete");
        System.out.println("[INFO]very good! You own Minecraft! Login is now complete, I wish you a happy playing!");

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(get_profile_url);
        method.addRequestHeader("Authorization", "Bearer " + MinecraftToken);

        try {
            int code = client.executeMethod(method);

            if (code == HttpURLConnection.HTTP_OK) {
                String body = method.getResponseBodyAsString();
                JsonObject resp_obj = new Gson().fromJson(body, JsonObject.class);
                File user_config_file = new File(System.getProperty("user.dir") + "/.openmlauncher/user.json");
                try {
                    String user_config_str = "";
                    if (!user_config_file.exists()) {
                        user_config_str = "{}";
                    } else {
                        System.out.println("[INFO]Reading 'user.json' file");
                        user_config_str = FileUtils.readFileToString(user_config_file, "utf-8");
                        System.out.println("[INFO]Read 'user.json' file complete");
                    }
                    JsonObject config_obj = new Gson().fromJson(user_config_str, JsonObject.class);
                    JsonObject new_arr_obj = new Gson().fromJson("{}", JsonObject.class);
                    new_arr_obj.addProperty("type", "mojang");
                    new_arr_obj.add("profile", resp_obj);
                    new_arr_obj.addProperty("minecraft_token", MinecraftToken);
                    new_arr_obj.addProperty("microsoft_refresh_token", refresh_token);
                    if (config_obj.has("users")) {
                        if (!config_obj.getAsJsonArray("users").isEmpty()) {
                            for (int i = 0; i < config_obj.get("users").getAsJsonArray().size(); i++) {
                                JsonObject profile = config_obj.get("users").getAsJsonArray()
                                        .get(i).getAsJsonObject()
                                        .get("profile").getAsJsonObject();
                                if (i != config_obj.get("users").getAsJsonArray().size() - 1) {
                                    if (
                                            profile.get("id").getAsString()
                                                    .equals(
                                                            resp_obj.get("id").getAsString()
                                                    )
                                                    &&
                                                    config_obj.get("users").getAsJsonArray()
                                                            .get(i).getAsJsonObject()
                                                            .get("type").getAsString()
                                                            .equals("microsoft")
                                    ) {
                                        break;
                                    }
                                } else {
                                    if (
                                            !profile.get("id").getAsString()
                                                    .equals(
                                                            resp_obj.get("id").getAsString()
                                                    )
                                    ) {
                                        config_obj.get("users").getAsJsonArray().add(new_arr_obj);
                                    }
                                }

                            }
                        } else {
                            config_obj.get("users").getAsJsonArray().add(new_arr_obj);
                        }
                        System.out.println("[INFO]Writing 'user.json' file");
                        FileUtils.writeStringToFile(user_config_file, config_obj.toString(), "utf-8");
                        System.out.println("[INFO]Write 'user.json' file complete");
                    } else {
                        JsonArray arr = new Gson().fromJson("[]", JsonArray.class);
                        arr.add(new_arr_obj);
                        config_obj.add("users", arr);
                        config_obj.addProperty("selector", 0);
                        FileUtils.writeStringToFile(user_config_file, config_obj.toString(), "utf-8");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Bad Connection:" + code);
            }
            fireWorkspaceStarted("OAuthFinish");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
