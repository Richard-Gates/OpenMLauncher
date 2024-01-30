package cn.goindog.openmlauncher.test;

import cn.goindog.OpenMLauncher.account.Microsoft.*;

import java.io.IOException;
import java.net.URI;

public class NewMicrosoftControllerTest {
    public static void main(String[] args) {
        MicrosoftOAuthOptions options = new MicrosoftOAuthOptions();
        options.addScope("XboxLive.signin");
        options.addScope("offline_access");
        options.setOAuthType(MicrosoftOAuthType.DEVICE);

        options.setMethod(new MicrosoftOAuthCodeMethod() {
            @Override
            public void GetMSCode(URI uri) {
                try {
                    java.awt.Desktop.getDesktop().browse(uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        MicrosoftController controller = new MicrosoftController();
        controller.build(options);
    }
}
