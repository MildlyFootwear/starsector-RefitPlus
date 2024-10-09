package Shoey.RefitPlus;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

import static Shoey.RefitPlus.MainPlugin.*;

public class RefitLabelUpdateThread implements Runnable {

//
// Doing reflections in a separate thread will not impact performance of the game.
// Just need to implement appropriate logic to not let the thread run when not needed.
//


    Logger log = Global.getLogger(this.getClass());
    float x = 250;
    float y = 0;
    List<String> knownHooks = new ArrayList<>();
    int w = 0;
    boolean needsTermination = false;
    ShipAPI ship;
    MutableShipStatsAPI stats;

    String label;
    String value;

    void UpdateCrew()
    {
        label = LabelNames.get(0);
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
        if (!LabelNameTiedValue.containsKey(label) || value != LabelNameTiedValue.get(label)) {
            LabelNameTiedValue.put(label, value);
            LabelAPI l = LabelNameTiedValueLabel.get(label);
            if (temp < 1)
                l.setColor(Global.getSettings().getColor("mountGreenColor"));
            else if (temp > 1)
                l.setColor(Global.getSettings().getColor("mountOrangeColor"));
            else
                l.setColor(Global.getSettings().getColor("yellowTextColor"));

            l.setText(value);
            l.autoSizeToWidth(l.computeTextWidth(value));
        }
    }

    void UpdateRecovery()
    {
        label = LabelNames.get(1);
        int temp =(100-(Math.round(stats.getBreakProb().getModifiedValue()*100)));
        value = ""+temp;
        value += "%";

        if (!LabelNameTiedValue.containsKey(label) || value != LabelNameTiedValue.get(label)) {
            LabelNameTiedValue.put(label, value);
            LabelAPI l = LabelNameTiedValueLabel.get(label);
            l.setText(value);
            l.autoSizeToWidth(l.computeTextWidth(value));

            if (temp == 100)
                l.setColor(Global.getSettings().getColor("mountGreenColor"));
            else if (temp > 75)
                l.setColor(Global.getSettings().getColor("yellowTextColor"));
            else
                l.setColor(Global.getSector().getPlayerFaction().getRelColor(RepLevel.VENGEFUL));
        }

    }

    public void updateStats()
    {
        ship = RPReflectInstance.getRefitShipAPI();
        if (ship != null) {

            stats = ship.getMutableStats();
            UpdateCrew();
            UpdateRecovery();

        }
    }

    public void updatePositions()
    {
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
        timesSkipped = 0;
        timesNot = 0;
        while (GameState.CAMPAIGN == Global.getCurrentState() && EveryFrameChecks.last == CoreUITabId.REFIT && !needsTermination) {
            EveryFrameChecks.newFrame = false;
            RPReflectInstance.hookRefit();
            updateStats();
            updatePositions();
            w = 0;


            try {
                while (!EveryFrameChecks.newFrame && w < 300) {
                    w++;
                    Thread.sleep(1);
                }
            } catch (Exception e) {
            }
        }
        log.debug("Overlay Thread: terminating. "+timesSkipped+" shortcuts taken vs "+timesNot);
    }
}
