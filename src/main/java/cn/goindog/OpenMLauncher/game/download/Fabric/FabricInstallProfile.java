package cn.goindog.OpenMLauncher.game.download.Fabric;

public class FabricInstallProfile {
    private String gameVer;
    private String fabricVer;

    public String getFabricVersion() {
        return fabricVer;
    }

    public String getGameVersion() {
        return gameVer;
    }

    public FabricInstallProfile setFabricVersion(String version) {
        this.fabricVer = version;
        return this;
    }

    public FabricInstallProfile setGameVersion(String version) {
        this.gameVer = version;
        return this;
    }
}
