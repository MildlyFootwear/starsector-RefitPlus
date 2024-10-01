package Shoey.RefitPlus;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.lang.System;


public class MainPlugin extends BaseModPlugin {

    private Logger log = Global.getLogger(this.getClass());

    public static SectorAPI sector = null;
    public static CampaignUIAPI cUI = null;
    public static ShipAPI ship = null;
    public static CampaignFleetAPI playerFleet = null;
    public static CombatEngineAPI combatEngine = null;
    public static float refitShipSelecterTimer = 0;

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        log.setLevel(Level.INFO);
    }

    @Override
    public void onGameLoad(boolean b) {
        super.onGameLoad(b);
        sector = Global.getSector();
        cUI = sector.getCampaignUI();
        playerFleet = sector.getPlayerFleet();
        sector.getListenerManager().addListener(new CampaignRefitRenderer(), true);
    }

    @Override
    public void beforeGameSave()
    {
        super.beforeGameSave();
    }

    @Override
    public void afterGameSave()
    {
        super.afterGameSave();
    }
}
