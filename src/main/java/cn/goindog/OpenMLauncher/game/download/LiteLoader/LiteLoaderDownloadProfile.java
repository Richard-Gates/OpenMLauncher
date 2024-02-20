package cn.goindog.OpenMLauncher.game.download.LiteLoader;

import cn.goindog.OpenMLauncher.exceptions.GameExceptions.UnknownGameVersionException;

public class LiteLoaderDownloadProfile {
    private String vanillaVersion;
    private String liteloaderVersion;

    public String getVanillaVersion() {
        return this.vanillaVersion;
    }

    public LiteLoaderDownloadProfile setVanillaVersion(String version) {
        this.vanillaVersion = version;
        return this;
    }
    public String getLoaderVersion() {
        return this.liteloaderVersion;
    }

    public LiteLoaderDownloadProfile setLoaderVersion(String version) {
        this.liteloaderVersion = version;
        return this;
    }

    public boolean isSnapshot() throws UnknownGameVersionException {
        if (getLoaderVersion().isEmpty()) {
            throw new UnknownGameVersionException("The Loader Version is null!");
        }

        return getLoaderVersion().contains("SNAPSHOT");
    }
}
