import cn.goindog.OpenMLauncher.account.Microsoft.MicrosoftController;
import cn.goindog.OpenMLauncher.account.Microsoft.MicrosoftOAuthCodeMethod;
import cn.goindog.OpenMLauncher.account.Microsoft.MicrosoftOAuthOptions;
import cn.goindog.OpenMLauncher.account.Microsoft.MicrosoftOAuthType;
import cn.goindog.OpenMLauncher.account.Offline.OfflineController;
import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaDownloader;
import cn.goindog.OpenMLauncher.game.download.Vanilla.VanillaInstallProfile;

import java.io.IOException;
import java.net.URI;

public class OfflineControllerTest {
    public static void main(String[] args) throws IOException {
        MicrosoftOAuthOptions options = new MicrosoftOAuthOptions();
        options.setOAuthType(MicrosoftOAuthType.AUTHORIZATION_CODE);
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

        OfflineController offlineController = new OfflineController();
        offlineController.build("Richard Gates");
    }
}
