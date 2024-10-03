package Shoey.RefitPlus;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class MainPlugin extends BaseModPlugin {

    private Logger log = Global.getLogger(this.getClass());

    public static SectorAPI sector = null;
    public static CampaignUIAPI cUI = null;
    public static ShipAPI ship = null;
    public static CampaignFleetAPI playerFleet = null;
    public static Level logLevel = Level.DEBUG;

    public static boolean RefitHooked;
    public static boolean needOverlayPlacement = true;
    public static boolean KotlinWait = false;

    public static CampaignRefitRenderer cRR;

    public static UIPanelAPI refit = null;

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
        playerFleet = sector.getPlayerFleet();
        cRR = new CampaignRefitRenderer();
        sector.getListenerManager().removeListenerOfClass(CampaignRefitRenderer.class);
        sector.getListenerManager().addListener(cRR, true);
    }

    @Override
    public void beforeGameSave() {
        super.beforeGameSave();
    }

    @Override
    public void afterGameSave() {
        super.afterGameSave();
    }
}
