package cn.goindog.OpenMLauncher.account.Microsoft;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.Objects;

public class MicrosoftOAuthOptions {
    private final JsonArray scopes = new JsonArray();
    private MicrosoftOAuthType type = MicrosoftOAuthType.DEVICE;
    private MicrosoftOAuthCodeMethod method;

    /**
     * 获取OAuth Scope参数
     * @return 以String[]形式返回scope数组
     */
    public String[] getScopes() {
        return new Gson().fromJson(scopes, String[].class);
    }

    /**
     * 获取当前登录方式（设备代码流/授权代码流）
     * @return 以MicrosoftOAuthType返回登录方式
     */
    public MicrosoftOAuthType getType() {
        return type;
    }

    /**
     * 当登录方式为授权代码流时，获取授权代码流登录页面显示方式
     * @return 返回显示方式抽象类
     */
    public MicrosoftOAuthCodeMethod getMethod() {
        return method;
    }

    /**
     * 当登录方式为授权代码流时，设置授权代码流显示方式
     * @param method 授权代码流显示方式抽象类
     * @return 返回设置后的登录选项类
     */
    public MicrosoftOAuthOptions setMethod(MicrosoftOAuthCodeMethod method) {
        this.method = method;
        return this;
    }

    /**
     * 添加OAuth Scope参数
     * @param scope 被添加的Scope参数
     * @return 添加参数后的登录选项类
     */
    public MicrosoftOAuthOptions addScope(String scope) {
        scopes.add(scope);
        return this;
    }

    /**
     * 移除OAuth Scope类
     * @param scope 被移除的Scope
     * @return 返回移除参数后的登录选项类
     */
    public MicrosoftOAuthOptions removeScope(String scope) {
        for (int i = 0; i < scopes.size(); i++) {
            if (
                    Objects.equals(scopes.get(i).getAsString(), scope)
            ) {
                scopes.remove(i);
            }
        }
        return this;
    }

    /**
     * 设置OAuth类型
     * @param type OAuth类型（设备代码流 - DEVICE 或 授权代码流 - AUTHORIZATION_CODE）
     * @return 返回设置OAuth类型后的登录选项类
     */
    public MicrosoftOAuthOptions setOAuthType(MicrosoftOAuthType type) {
        this.type = type;
        return this;
    }

}