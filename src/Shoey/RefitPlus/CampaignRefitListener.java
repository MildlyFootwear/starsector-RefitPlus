package Shoey.RefitPlus;

import Shoey.RefitPlus.Kotlin.Refit;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static Shoey.RefitPlus.MainPlugin.*;


public class CampaignRefitListener implements CoreUITabListener {

    Logger log = Global.getLogger(this.getClass());
    public boolean init;
    public boolean Wait = false;
    public boolean WaitPrinted = false;
    public boolean RehookInCheck = false;
    public ShipAPI ship = null;
    public static Refit RefitInstance = new Refit();
    public List<LabelAPI> labels = new ArrayList<>();
    public UIPanelAPI lastRefit;
    public MutableShipStatsAPI stats;
    public UIPanelAPI coreUI = null;
    public UIPanelAPI refit = null;
    public HashMap<String, String> LabelNameTiedValue = new HashMap<>();
    public HashMap<String, LabelAPI> LabelNameTiedValueLabel = new HashMap<>();
    public List<String> LabelNames = Arrays.asList(new String[]{"Crew Loss", "DPS", ""});

    void updateLabels()
    {
        ship = RefitInstance.getRefitShipAPI();
        if (ship != null)
        {
            String label;
            String value;
            stats = ship.getMutableStats();
            stats.getBallisticWeaponRangeBonus();

            label = LabelNames.get(0);
            value = ((float) (Math.round(stats.getCrewLossMult().getModifiedValue()*1000))/1000)+"x";
            if (!LabelNameTiedValue.containsKey(label) || value != LabelNameTiedValue.get(label))
            {
                LabelNameTiedValue.put(label, value);
                LabelAPI l = LabelNameTiedValueLabel.get(label);
                l.setText(value);
                l.autoSizeToWidth(l.computeTextWidth(value));
            }
            label = LabelNames.get(1);
            float dps = 0;
            for (WeaponAPI w : ship.getAllWeapons()) {
                dps += w.getDamage().getStats().getBallisticWeaponDamageMult().getModifiedValue();
            }
            value = ""+Math.round(dps);
            if (!LabelNameTiedValue.containsKey(label) || value != LabelNameTiedValue.get(label))
            {
                LabelNameTiedValue.put(label, value);
                LabelAPI l = LabelNameTiedValueLabel.get(label);
                l.setText(value);
                l.autoSizeToWidth(l.computeTextWidth(value));
            }
        }
    }

    void clearRefitVars()
    {
        refit = null;
        RefitHooked = false;
    }

    void hookIfNeeded()
    {
        if (KotlinWait)
            return;
        KotlinWait = true;
        RefitInstance.getRefit(refit == null || !RefitHooked);
    }

    void InsertOverlay(String s) {

        if (!s.isEmpty())
            log.debug("Executing InsertOverlay from "+s);

        hookIfNeeded();

        if (RefitHooked && lastRefit != refit && refit != null) {
            try {
                for (LabelAPI l : labels) {
                    refit.addComponent((UIComponentAPI) l);
                }
                for (LabelAPI l : LabelNameTiedValueLabel.values()) {
                    refit.addComponent((UIComponentAPI) l);
                }
                log.info("Added labels");
                lastRefit = refit;
                needOverlayPlacement = false;
            } catch (Exception e) {
                log.debug(e.getMessage());
                needOverlayPlacement = true;
            }
        } else if (lastRefit == refit && lastRefit != null) {
        } else {
            clearRefitVars();
            needOverlayPlacement = true;
            log.error("Refit not hooked.");
        }
    }

    void InsertOverlay()
    {
        InsertOverlay("");
    }

    void FrameChecks(float amount) {

        if (!init) {
            init = true;
            log.setLevel(logLevel);
            for (String s : LabelNames)
            {
                LabelAPI l = Global.getSettings().createLabel(s, Fonts.VICTOR_10);
                labels.add(l);
                if (labels.size() > 1)
                    l.getPosition().rightOfMid((UIComponentAPI) labels.get(labels.size()-2), 1000);
                log.debug(s+" x " +l.getPosition().getX() + " y " + l.getPosition().getY());
                LabelAPI l2 = Global.getSettings().createLabel("", Fonts.ORBITRON_12);
                l2.getPosition().aboveRight((UIComponentAPI) l, 0);
                LabelNameTiedValueLabel.put(s, l);
            }
            log.debug("Campaign listener initialized");
        }

        if (KotlinWait) {
            return;
        }

        if (sector == null || cUI == null || cUI.getCurrentCoreTab() != CoreUITabId.REFIT || Wait) {
            if (Wait)
            {
                if (!WaitPrinted) {
                    log.debug("Waiting...");
                    WaitPrinted = true;
                }
                RehookInCheck = true;
            }
            RefitHooked = false;
            return;
        }

        WaitPrinted = false;

        if (lastRefit != refit)
        {
            needOverlayPlacement = true;
            log.debug("lastRefit is not refit");
        }

        if (needOverlayPlacement)
            InsertOverlay();

        updateLabels();

    }

    @Override
    public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
        RefitInstance.hookCore();
        if (tab != CoreUITabId.REFIT)
            return;
        log.debug("Clearing variables as opening prep");
        refitTimerScript.loopcount = 0;
        clearRefitVars();
    }
}
