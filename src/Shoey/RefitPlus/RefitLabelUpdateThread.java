package Shoey.RefitPlus;

import Shoey.RefitPlus.Kotlin.Refit;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

import static Shoey.RefitPlus.MainPlugin.*;

public class RefitLabelUpdateThread implements Runnable {

    Logger log = Global.getLogger(this.getClass());
    Refit RefitInstance = new Refit();
    float x = 250;
    float y = 0;
    List<String> knownHooks = new ArrayList<>();

    public void updateStats()
    {
        ShipAPI ship = RefitInstance.getRefitShipAPI();
        if (!knownHooks.contains(ship.getId())) {
            knownHooks.add(ship.getId());
            log.debug("ID found "+ship.getId());
        }
        if (ship != null) {
            String label;
            String value;
            MutableShipStatsAPI stats = ship.getMutableStats();
            stats.getBallisticWeaponRangeBonus();

            label = cRL.LabelNames.get(0);
            float temp = (float) (Math.round(stats.getCrewLossMult().getModifiedValue() * 100)) / 100;
            try {
                value = ""+temp;
                while (value.length() < 4)
                {
                    value += "0";
                }
                value += "x";
            } catch (Exception e) {
                value = "You shouldn't see this.";
            }
            if (!cRL.LabelNameTiedValue.containsKey(label) || value != cRL.LabelNameTiedValue.get(label)) {
                cRL.LabelNameTiedValue.put(label, value);
                LabelAPI l = cRL.LabelNameTiedValueLabel.get(label);

                if (temp < 1)
                    l.setColor(sector.getPlayerFaction().getRelColor(RepLevel.COOPERATIVE));
                else if (temp > 1)
                    l.setColor(sector.getPlayerFaction().getRelColor(RepLevel.VENGEFUL));
                else
                    l.setColor(sector.getPlayerFaction().getRelColor(RepLevel.NEUTRAL));

                l.setText(value);
                l.autoSizeToWidth(l.computeTextWidth(value));
            }
            label = cRL.LabelNames.get(1);
            float bDPS = 0;
            for (WeaponAPI w : ship.getAllWeapons()) {
                MutableShipStatsAPI stat = w.getDamage().getStats();
                if (stat == null) {
                    continue;
                }
                if (stat.getBallisticWeaponDamageMult() != null) {
                    bDPS += w.getDamage().getStats().getBallisticWeaponDamageMult().getModifiedValue();
                }
            }
            value = "" + Math.round(bDPS);
            if (!cRL.LabelNameTiedValue.containsKey(label) || value != cRL.LabelNameTiedValue.get(label)) {
                cRL.LabelNameTiedValue.put(label, value);
                LabelAPI l = cRL.LabelNameTiedValueLabel.get(label);
                l.setText(value);
                l.autoSizeToWidth(l.computeTextWidth(value));
            }
        }
    }

    public void updatePositions()
    {
        cRL.labels.get(0).getPosition().inTR(265, 35);
        cRL.LabelNameTiedValueLabel.get(cRL.LabelNames.get(0)).getPosition().belowRight((UIComponentAPI) cRL.labels.get(0), 3);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD8)) {
            y++;
            log.debug("y "+y);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2)) {
            y--;
            log.debug("y "+y);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4)) {
            x++;
            log.debug("x "+x);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD6)) {
            x--;
            log.debug("x "+x);
        }
    }

    @Override
    public void run() {
        log.setLevel(logLevel);
        while (cUI.getCurrentCoreTab() != CoreUITabId.REFIT) {
            try {
                Thread.sleep(10);
                cRL.log.debug("Overlay Thread: waiting before ");
            } catch (InterruptedException e) {
            }
        }
        while (cUI.getCurrentCoreTab() == CoreUITabId.REFIT) {

            updateStats();
            updatePositions();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        cRL.log.debug("Overlay Thread: terminating");
    }
}
