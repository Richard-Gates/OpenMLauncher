package cn.goindog.OpenMLauncher.game.launch;

import cn.goindog.OpenMLauncher.exceptions.SystemExceptions.SystemNotSupportException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BedrockMCLauncher {
    public void build() throws SystemNotSupportException{
        try {
            String command = "cmd /c start minecraft://";
            System.out.println("[INFO]Bedrock Launcher: Running Minecraft Bedrock:(launchCommand)" + command);
            String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                int version = Integer.parseInt(System.getProperty("os.version"));
                if (version < 10) {
                    throw new SystemNotSupportException("The Windows Version (" + version + ") is not support");
                } else {
                    Process process = Runtime.getRuntime().exec(command);
                    String inStr = consumeInputStream(process.getInputStream());
                    int status = process.waitFor();
                    if (status == 0) {
                        System.out.println(inStr);
                    }
                }
            } else {
                throw new SystemNotSupportException("The Operating System (" + os + ") is not support");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    private String consumeInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null) {
            System.out.println(s);
            sb.append(s);
        }
        return sb.toString();
    }
}
