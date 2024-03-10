package cn.goindog.OpenMLauncher.game.download.Forge;

public class ForgeInstallProfile {
    private String gameVer;
    private String forgeVer;

    /**
     * 获取当前游戏版本
     * @param value 游戏版本
     * @return 设置游戏版本后的安装配置实例
     */
    public ForgeInstallProfile setGameVer(String value) {
        gameVer = value;
        return this;
    }

    /**
     * 获取当前游戏版本
     * @return 游戏版本
     */
    public String getGameVer() {
        return gameVer;
    }

    /**
     * 设置当前Forge版本
     * @param value Forge版本
     * @return 设置版本后的安装配实例
     */
    public ForgeInstallProfile setForgeVer(String value) {
        forgeVer = value;
        return this;
    }

    /**
     * 获取当前Forge版本
     * @return 当前Forge版本
     */
    public String getForgeVer() {
        return forgeVer;
    }
}
