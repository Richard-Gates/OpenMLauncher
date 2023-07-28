package cn.goindog.OpenMLauncher.game;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;

public class VanillaController{
    private static final String manifest_url = "https://piston-meta.mojang.com/mc/game/version_manifest.json";

    public JsonArray getAllGameVersion() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(manifest_url).openConnection();
        connection.connect();
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder resp = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                resp.append(line);
                resp.append("\n\r");
            }
            JsonArray arr = new Gson().fromJson(resp.toString(), JsonObject.class).getAsJsonArray("versions");
            return arr;
        }
        return null;
    }
    public void Get(String versionName) throws IOException {
        System.out.println("[INFO]Reading 'version_manifest.json' file.");
        HttpURLConnection vm_connection = (HttpURLConnection) new URL(manifest_url).openConnection();
        vm_connection.setRequestMethod("GET");
        vm_connection.connect();
        if (vm_connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader vm_reader = new BufferedReader(new InputStreamReader(vm_connection.getInputStream()));
            StringBuilder vm_builder = new StringBuilder();
            while
        }
    }

    public static void main(String[] args) throws IOException {
        new VanillaController().Get("1.20.1");
    }
}
