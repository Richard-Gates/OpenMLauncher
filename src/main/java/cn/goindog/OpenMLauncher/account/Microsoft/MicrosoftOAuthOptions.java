package cn.goindog.OpenMLauncher.account.Microsoft;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.Objects;

public class MicrosoftOAuthOptions {
    private final JsonArray scopes = new JsonArray();
    private MicrosoftOAuthType type = MicrosoftOAuthType.DEVICE;
    private MicrosoftOAuthCodeMethod method;

    public String[] getScopes() {
        return new Gson().fromJson(scopes, String[].class);
    }

    public MicrosoftOAuthType getType() {
        return type;
    }

    public MicrosoftOAuthCodeMethod getMethod() {
        return method;
    }

    public void setMethod(MicrosoftOAuthCodeMethod method) {
        this.method = method;
    }

    public void addScope(String scope) {
        scopes.add(scope);
    }

    public void removeScope(String scope) {
        for (int i = 0; i < scopes.size(); i++) {
            if (
                    Objects.equals(scopes.get(i).getAsString(), scope)
            ) {
                scopes.remove(i);
            }
        }
    }

    public void setOAuthType(MicrosoftOAuthType type) {
        this.type = type;
    }

}