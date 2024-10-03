package Shoey.RefitPlus;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class MainPlugin extends BaseModPlugin {

    private Logger log = Global.getLogger(this.getClass());

    public static SectorAPI sector = null;
    public static CampaignUIAPI cUI = null;
    public static Level logLevel = Level.DEBUG;

    public static boolean RefitHooked;
    public static boolean needOverlayPlacement = true;
    public static boolean KotlinWait = false;

    public static CampaignRefitListener cRL;

    public static int reflectionCount = 0;

    public static String UIDump;

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        log.setLevel(logLevel);
    }

    @Override
    public void onGameLoad(boolean b) {
        super.onGameLoad(b);
        sector = Global.getSector();
        cUI = sector.getCampaignUI();
        cRL = new CampaignRefitListener();
        sector.getListenerManager().removeListenerOfClass(CampaignRefitListener.class);
        sector.getListenerManager().addListener(cRL, true);
        sector.addTransientScript(new EveryFrameChecks());
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void beforeGameSave() {
        super.beforeGameSave();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void afterGameSave() {
        super.afterGameSave();
    }
}
