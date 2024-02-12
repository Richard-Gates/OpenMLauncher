package cn.goindog.OpenMLauncher.game.launch;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BedrockMCLauncher {
    public void build() {
        try {
            String command = "cmd /c start minecraft://";
            System.out.println("[INFO]Bedrock Launcher: Running Minecraft Bedrock:(launchCommand)" + command);
            Process process = Runtime.getRuntime().exec(command);
            String inStr = consumeInputStream(process.getInputStream());
            int status = process.waitFor();
            if (status == 0) {
                System.out.println(inStr);
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
