package cn.goindog.OpenMLauncher.account.Microsoft;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MicrosoftController {
    private static final String live_service_url = "https://login.live.com/oauth20_authorize.srf?client_id=8073fcb7-1de0-4440-b6d3-62f8407bd5dc&scope=XboxLive.signin&response_type=code";
    private static final String code_to_token_url = "https://login.live.com/oauth20_token.srf";
    private static final String xbl_url = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String xsts_url = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String Minecraft_Authenticate_Url = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String Check_Url = "https://api.minecraftservices.com/entitlements/mcstore";
    private static final String get_profile_url = "https://api.minecraftservices.com/minecraft/profile";
    private static final HttpServer server;

    static {
        try {
            server = HttpServer.create(new InetSocketAddress(3217), 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MicrosoftController() throws IOException {
    }

    public void AddAccount() throws IOException {
        GetCode();
    }

    private static void GetCode() throws IOException {
        final String[] code = {""};
        server.createContext("/", exchange -> {
            code[0] = exchange.getRequestURI().getQuery().replaceFirst("code=", "");
            exchange.setAttribute("Content-Type", "text/html");
            String response = """
                    <script>
                    window.close()
                    </script>
                    """;
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
            CodeToToken(code);
        });
        server.start();
        java.awt.Desktop.getDesktop().browse(URI.create(live_service_url));
        System.out.println("[INFO]Getting Microsoft Account Code");
    }

    private static void CodeToToken(String[] code) {
        System.out.println("[INFO]Get Microsoft Account Code Complete");
        System.out.println("[INFO]Getting Microsoft Account Token");
        Map<Object, Object> c_t_t_data = Map.of(
                "client_id", "8073fcb7-1de0-4440-b6d3-62f8407bd5dc",
                "code", code[0],
                "scope", "XboxLive.signin",
                "grant_type", "authorization_code"
        );
        HttpRequest c_t_t_request = HttpRequest.newBuilder(URI.create(code_to_token_url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(ofFormData(c_t_t_data)).build();
        HttpClient.newBuilder().build().sendAsync(c_t_t_request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
            if (resp.statusCode() == HttpURLConnection.HTTP_OK) {
                String body = resp.body();
                JsonObject response_obj = new Gson().fromJson(body, JsonObject.class);
                String ms_token = response_obj.get("access_token").getAsString();
                XblAuthenticate(ms_token);
            } else {
                System.out.println("Bad Connection:" + resp.statusCode());
            }
        });
    }

    private static void XblAuthenticate(String ms_token) {
        System.out.println("[INFO]Get Microsoft Account Token Complete");
        System.out.println("[INFO]Getting XBox Live Token & UserHash");
        Map<Object, Object> xbl_dat = Map.of(
                "Properties", Map.of(
                        "AuthMethod", "RPS",
                        "SiteName", "user.auth.xboxlive.com",
                        "RpsTicket", "d=" + ms_token
                ),
                "RelyingParty", "http://auth.xboxlive.com",
                "TokenType", "JWT"
        );
        HttpRequest xbl_req = HttpRequest.newBuilder(URI.create(xbl_url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(ofJsonData(xbl_dat))
                .build();
        HttpClient.newBuilder().build().sendAsync(xbl_req, HttpResponse.BodyHandlers.ofString()).thenAccept(xbl_resp -> {
            if (xbl_resp.statusCode() == HttpURLConnection.HTTP_OK) {
                String xbl_body = xbl_resp.body();
                JsonObject xbl_resp_obj = new Gson().fromJson(xbl_body, JsonObject.class);
                String xbl_token = xbl_resp_obj.get("Token").getAsString();
                String xbl_uhs = xbl_resp_obj.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
                try {
                    XstsAuthenticate(xbl_token, xbl_uhs);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Bad Connection:" + xbl_resp.statusCode());
            }
        });
    }

    private static void XstsAuthenticate(String XblToken, String XblUhs) throws URISyntaxException {
        System.out.println("[INFO]Get XBox Live Token & UserHash Complete");
        System.out.println("[INFO]Getting XSTS Token & UserHash");
        Map<Object, Object> dat = Map.of(
                "Properties", Map.of(
                        "SandboxId", "RETAIL",
                        "UserTokens", List.of(XblToken)
                ),
                "RelyingParty", "rp://api.minecraftservices.com/",
                "TokenType", "JWT"
        );
        HttpRequest request = HttpRequest.newBuilder(URI.create(xsts_url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(ofJsonData(dat)).build();
        HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
            if (resp.statusCode() == HttpURLConnection.HTTP_OK) {
                String body = resp.body();
                JsonObject resp_obj = new Gson().fromJson(body, JsonObject.class);
                String xsts_token = resp_obj.get("Token").getAsString();
                String xsts_uhs = resp_obj.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
                MinecraftAuthenticate(xsts_token, xsts_uhs);
            } else {
                System.out.println("Bad Connection:" + resp.statusCode());
            }
        });
    }

    private static void MinecraftAuthenticate(String XSTSToken, String UHS) {
        System.out.println("[INFO]Get XSTS Token & UserHash Complete");
        System.out.println("[INFO]Getting Minecraft Account Token & UUID");
        Map<Object, Object> dat = Map.of(
                "identityToken", "XBL3.0 x=" + UHS + ";" + XSTSToken
        );
        HttpRequest request = HttpRequest.newBuilder(URI.create(Minecraft_Authenticate_Url))
                .POST(ofJsonData(dat))
                .build();
        HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
            if (resp.statusCode() == HttpURLConnection.HTTP_OK) {
                String body = resp.body();
                JsonObject resp_obj = new Gson().fromJson(body, JsonObject.class);
                String mc_token = resp_obj.get("access_token").getAsString();
                CheckHaveMinecraft(mc_token);
            } else {
                System.out.println("Bad Connection:" + resp.statusCode());
            }
        });
    }

    private static void CheckHaveMinecraft(String MinecraftToken) {
        System.out.println("[INFO]Get Minecraft Account Token & UUID Complete");
        System.out.println("[INFO]Checking Minecraft Possession");
        HttpRequest request = HttpRequest.newBuilder(URI.create(Check_Url))
                .header("Authorization", "Bearer " + MinecraftToken)
                .GET()
                .build();
        HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
            if (resp.statusCode() == HttpURLConnection.HTTP_OK) {
                String body = resp.body();
                JsonObject resp_obj = new Gson().fromJson(body, JsonObject.class);
                if (resp_obj.has("items")) {
                    try {
                        GetProfile(MinecraftToken);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("[WARN]Failed to complete the login!");
                    System.out.println("[ERROR]Sorry, your Microsoft account does not have Minecraft, failed to complete the login!");
                }
            } else {
                System.out.println("Bad Connection:" + resp.statusCode());
            }
        });
    }

    private static void GetProfile(String MinecraftToken) throws InterruptedException {
        System.out.println("[INFO]Check Minecraft Possession Complete");
        System.out.println("[INFO]very good! You own Minecraft! Login is now complete, I wish you a happy playing!");
        HttpRequest request = HttpRequest.newBuilder(URI.create(get_profile_url))
                .header("Authorization", "Bearer " + MinecraftToken)
                .GET()
                .build();
        HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
            if (resp.statusCode() == HttpURLConnection.HTTP_OK) {
                String body = resp.body();
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
                    new_arr_obj.addProperty("type", "microsoft");
                    resp_obj.addProperty("token", MinecraftToken);
                    new_arr_obj.add("profile", resp_obj);
                    if (config_obj.has("users")) {
                        for (int i = 0; i < config_obj.get("users").getAsJsonArray().size(); i++) {
                            if (i != config_obj.get("users").getAsJsonArray().size()) {
                                if (config_obj.get("users").getAsJsonArray().get(i).getAsJsonObject().get("name").getAsString() == resp_obj.get("name").getAsString()) {
                                    break;
                                }
                            } else {
                                if (config_obj.get("users").getAsJsonArray().get(i).getAsJsonObject().get("name").getAsString() == resp_obj.get("name").getAsString()) {
                                    break;
                                } else {
                                    config_obj.get("users").getAsJsonArray().add(new_arr_obj);
                                }
                            }
                        }
                        System.out.println("[INFO]Writing 'user.json' file");
                        FileUtils.writeStringToFile(user_config_file, config_obj.toString(), "utf-8");
                        System.out.println("[INFO]Write 'user.json' file complete");
                    } else {
                        JsonArray arr = new Gson().fromJson("[]", JsonArray.class);
                        arr.add(new_arr_obj);
                        config_obj.add("users", arr);
                        FileUtils.writeStringToFile(user_config_file, config_obj.toString(), "utf-8");
                    }
                    server.stop(0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Bad Connection:" + resp.statusCode());
            }
        });
    }

    public static HttpRequest.BodyPublisher ofJsonData(Map<Object, Object> dat) {
        String str = new Gson().fromJson(new Gson().toJson(dat), JsonObject.class).toString();
        return HttpRequest.BodyPublishers.ofString(str);
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}