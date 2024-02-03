package cn.goindog.OpenMLauncher.game.download.Forge;

public class ForgeInstallProfile {
    private String gameVer;
    private String forgeVer;

    public ForgeInstallProfile setGameVer(String value) {
        gameVer = value;
        return this;
    }
    public String getGameVer() {
        return gameVer;
    }
    public ForgeInstallProfile setForgeVer(String value) {
        forgeVer = value;
        return this;
    }
    public String getForgeVer() {
        return forgeVer;
    }
}
