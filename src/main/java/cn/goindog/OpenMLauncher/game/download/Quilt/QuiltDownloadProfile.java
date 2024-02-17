package cn.goindog.OpenMLauncher.game.download.Quilt;

public class QuiltDownloadProfile {
    private String vanillaVersion;
    private String quiltVersion;

    public String getVanillaVersion() {
        return vanillaVersion;
    }

    public String getQuiltVersion() {
        return quiltVersion;
    }

    public QuiltDownloadProfile setVanillaVersion(String vanillaVersion) {
        this.vanillaVersion = vanillaVersion;
        return this;
    }

    public QuiltDownloadProfile setQuiltVersion(String quiltVersion) {
        this.quiltVersion = quiltVersion;
        return this;
    }
}
