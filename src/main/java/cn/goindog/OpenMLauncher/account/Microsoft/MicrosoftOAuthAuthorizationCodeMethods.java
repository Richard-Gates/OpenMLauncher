package cn.goindog.OpenMLauncher.account.Microsoft;

import cn.goindog.OpenMLauncher.events.OAuthEvents.OAuthFinishEventListener;
import cn.goindog.OpenMLauncher.events.OAuthEvents.OAuthFinishEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.HttpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class MicrosoftOAuthAuthorizationCodeMethods {

    private static final HttpServer server;

    static {
        try {
            server = HttpServer.create(new InetSocketAddress(3217), 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String live_service_url = "https://login.live.com/oauth20_authorize.srf?client_id=8073fcb7-1de0-4440-b6d3-62f8407bd5dc&scope=XboxLive.signin%20offline_access&response_type=code";
    private static final String code_to_token_url = "https://login.live.com/oauth20_token.srf";

    private final JsonObject object = new JsonObject();
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

    /**
     * 使用授权代码流登录
     * @param method 授权代码流登录界面显示方法实例
     */
    public void build(MicrosoftOAuthCodeMethod method) {
        GetCode(method);
    }

    public JsonObject getObject() {
        return object;
    }

    private void GetCode(MicrosoftOAuthCodeMethod method) {
        final String[] code = {""};
        server.createContext("/", exchange -> {
            exchange.setAttribute("Content-Type", "text/html");
            String response = """
                    <script>
                    window.close()
                    </script>
                    """;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream rb = exchange.getResponseBody();
            rb.write(response.getBytes());
            rb.close();
            code[0] = exchange.getRequestURI().getQuery().replaceFirst("code=", "");
            CodeToToken(code[0]);
            server.stop(0);
        });
        server.start();
        method.GetMSCode(
                URI.create(live_service_url)
        );
        System.out.println("[INFO]Getting Microsoft Account Code");
    }

    private void CodeToToken(String code) throws IOException {
        System.out.println("[INFO]Get Microsoft Account Code Complete");
        System.out.println("[INFO]Getting Microsoft Account Token");

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(code_to_token_url);

        method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");

        method.addParameter("client_id", "8073fcb7-1de0-4440-b6d3-62f8407bd5dc");
        method.addParameter("code", code);
        method.addParameter("scope", "XboxLive.signin offline_access");
        method.addParameter("grant_type", "authorization_code");

        int resp_code = client.executeMethod(method);

        if (resp_code == HttpURLConnection.HTTP_OK) {
            String body = method.getResponseBodyAsString();
            JsonObject response_obj = new Gson().fromJson(body, JsonObject.class);
            String access_token = response_obj.get("access_token").getAsString();

            object.addProperty("access_token", access_token);

            if (response_obj.has("refresh_token")) {
                object.addProperty("refresh_token", response_obj.get("refresh_token").getAsString());
            }

            System.out.println("[INFO]Get Microsoft Account Token Complete");
            fireWorkspaceStarted("OAuthFinish");
        } else {
            System.out.println("Bad Connection:" + resp_code);
        }
    }

}
