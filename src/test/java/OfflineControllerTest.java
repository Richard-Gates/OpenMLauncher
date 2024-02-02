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
    public static void main(String[] args) {
        OfflineController offlineController = new OfflineController();
        offlineController.build("Richard Gates");
    }
}
