package cn.goindog.OpenMLauncher.account.Microsoft;

import cn.goindog.OpenMLauncher.events.OAuthEvents.OAuthFinishEvent;
import cn.goindog.OpenMLauncher.events.OAuthEvents.OAuthFinishEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.httpclient.methods.PostMethod;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class MicrosoftOAuthDeviceFlowMethods {

    private static final String device_code_get_url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    private static final String device_method_token_get_url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private final JsonObject object = new JsonObject();
    private Collection listeners;

    /**
     * 添加OAuth完成事件监听器
     * @param listener 监听器实例
     */
    public void addOAuthFinishListener(OAuthFinishEventListener listener) {
        if (listeners == null) {
            listeners = new HashSet();
        }
        listeners.add(listener);
    }

    /**
     * 移除OAuth完成事件监听器实例
     * @param listener 监听器实例
     */
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
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            OAuthFinishEventListener listener = (OAuthFinishEventListener) iter.next();
            listener.OAuthFinishEvent(event);
        }
    }

    /**
     * 使用设备代码流登录
     * @param scopes OAuth Scope选项
     */
    public void build(String[] scopes) {
        DeviceGet(scopes);
    }

    public JsonObject getObject() {
        return object;
    }

    private void DeviceGet(String[] scopes) {
        String scopes_str = String.join(" ", scopes);
        System.out.println("[INFO]Microsoft Login Scopes:" + scopes_str.replace(" backspace ", " "));

        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
        PostMethod method = new PostMethod(device_code_get_url);

        method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");

        method.addParameter("client_id", "8073fcb7-1de0-4440-b6d3-62f8407bd5dc");
        method.addParameter("scope", scopes_str);

        try {
            int code = client.executeMethod(method);
            if (code == HttpURLConnection.HTTP_OK) {
                String result = method.getResponseBodyAsString();

                JsonObject response_object = new Gson().fromJson(result, JsonObject.class);

                String user_code = response_object.get("user_code").getAsString();
                String device_code = response_object.get("device_code").getAsString();
                String verification_uri = response_object.get("verification_uri").getAsString();
                int interval = response_object.get("interval").getAsInt();

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(
                        new StringSelection(user_code),
                        null
                );

                java.awt.Desktop.getDesktop().browse(new URI(verification_uri));

                DeviceCodeToToken(scopes, device_code, interval);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void DeviceCodeToToken(String[] scopes, String device_code, int interval) {
        String scopes_str = String.join(" ", scopes);

        AtomicReference<Boolean> bool = new AtomicReference<>(true);

        org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
        PostMethod method = new PostMethod(device_method_token_get_url);

        method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");

        method.addParameter("client_id", "8073fcb7-1de0-4440-b6d3-62f8407bd5dc");
        method.addParameter("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
        method.addParameter("scope", scopes_str);
        method.addParameter("code", device_code);

        try {
            while (bool.get()) {
                Thread.sleep(interval * 1000L);

                int code = httpClient.executeMethod(method);

                switch (code) {
                    case HttpURLConnection.HTTP_OK -> {
                        String body = method.getResponseBodyAsString();
                        JsonObject response = new Gson().fromJson(body, JsonObject.class);
                        String access_token = response.get("access_token").getAsString();
                        String refresh_token = response.get("refresh_token").getAsString();
                        object.addProperty("access_token", String.valueOf(access_token));
                        object.addProperty("refresh_token", String.valueOf(refresh_token));
                        bool.set(false);
                    }
                    case HttpURLConnection.HTTP_BAD_REQUEST -> {
                        String error = new Gson().fromJson(
                                        method.getResponseBodyAsString(),
                                        JsonObject.class)
                                .get("error").getAsString();
                        switch (error) {
                            case "authorization_declined":
                            case "expired_token": {
                                System.out.println("[INFO]Microsoft OAuth Device Code Flow Method: OAuth validation failed!");
                                bool.set(false);
                                break;
                            }
                            case "authorization_pending": {
                                System.out.println("[INFO]Microsoft OAuth Device Code Flow Method: OAuth verification is not complete!");
                                break;
                            }
                            case "bad_verification_code": {
                                System.out.println("[INFO]Microsoft OAuth Device Code Flow Method: OAuth \"device_code\" parameter error!");
                                break;
                            }
                        }
                    }
                    default ->
                            System.out.println("[INFO]Microsoft OAuth Device Code Flow Method: Bad Connection - " + code);
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[INFO]Get Microsoft Account Token Complete");
        fireWorkspaceStarted("OAuthFinish");
    }
}
