package cn.goindog.OpenMLauncher;

public class Injector {
    public static void main(String[] args) {
        if (System.getProperty("oml.gameDir").isEmpty()) {
            System.setProperty("oml.gameDir", System.getProperty("user.dir") + "/.minecraft/");
        }
    }
}
