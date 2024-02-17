package cn.goindog.OpenMLauncher.game.download.NeoForge;

public class NeoForgeDownloadProfile {
    private String vanillaVersion;
    private String neoforgeVersion;

    public String getNeoforgeVersion() {
        return neoforgeVersion;
    }

    public NeoForgeDownloadProfile setNeoforgeVersion(String neoforgeVersion) {
        this.neoforgeVersion = neoforgeVersion;
        return this;
    }

    public String getVanillaVersion() {
        return vanillaVersion;
    }

    public NeoForgeDownloadProfile setVanillaVersion(String vanillaVersion) {
        this.vanillaVersion = vanillaVersion;
        return this;
    }
}
