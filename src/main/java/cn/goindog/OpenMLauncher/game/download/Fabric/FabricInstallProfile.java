package cn.goindog.OpenMLauncher.game.download.Fabric;

public class FabricInstallProfile {
    private String gameVer;
    private String fabricVer;

    /**
     * 获取Fabric版本
     * @return 当前Fabric版本
     */
    public String getFabricVersion() {
        return fabricVer;
    }

    /**
     * 获取当前Minecraft版本
     * @return 当前Minecraft版本
     */
    public String getGameVersion() {
        return gameVer;
    }

    /**
     * 设置Fabric版本
     * @param version Fabric版本
     * @return 返回设置后的安装配置实例
     */
    public FabricInstallProfile setFabricVersion(String version) {
        this.fabricVer = version;
        return this;
    }

    /**
     * 设置Minecraft版本
     * @param version Minecraft版本
     * @return 返回设置后的安装配置实例
     */
    public FabricInstallProfile setGameVersion(String version) {
        this.gameVer = version;
        return this;
    }
}
