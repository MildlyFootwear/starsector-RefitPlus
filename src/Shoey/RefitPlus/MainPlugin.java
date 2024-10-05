package Shoey.RefitPlus;

import Shoey.RefitPlus.Kotlin.Dump;
import Shoey.RefitPlus.Kotlin.RPReflect;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MainPlugin extends BaseModPlugin {

    private Logger log = Global.getLogger(this.getClass());

    public static SectorAPI sector = null;
    public static CampaignUIAPI cUI = null;
    public static Level logLevel = Level.DEBUG;

    public static boolean needOverlayPlacement = true;
    public static boolean KotlinWait = false;

    public static int reflectionCount = 0;
    public static RPReflect RPReflectInstance = new RPReflect();
    public static List<LabelAPI> labels = new ArrayList<>();
    public static HashMap<String, String> LabelNameTiedValue = new HashMap<>();
    public static HashMap<String, LabelAPI> LabelNameTiedValueLabel = new HashMap<>();
    public static List<String> LabelNames = Arrays.asList("Crew Loss", "DPS", "");
    public static ShipAPI ship;
    public static UIPanelAPI refit = null;
    public static UIPanelAPI coreUI = null;
    public static Dump dump = new Dump();

    public static int timesSkipped = 0;
    public static int timesNot = 0;

    public static void insertOverlay()
    {

        LabelNameTiedValue.clear();

        for (LabelAPI l : labels) {
            refit.addComponent((UIComponentAPI) l);
        }
        for (LabelAPI l : LabelNameTiedValueLabel.values()) {
            refit.addComponent((UIComponentAPI) l);
        }

        labels.get(0).getPosition().inTR(265, 35);
        LabelNameTiedValueLabel.get(LabelNames.get(0)).getPosition().belowRight((UIComponentAPI) labels.get(0), 1);
    }

    @Override
    public void onApplicationLoad() throws Exception {

        super.onApplicationLoad();
        log.setLevel(logLevel);
        Global.getSettings().loadFont("graphics/fonts/orbitron16.fnt");
        for (String s : LabelNames)
        {
            LabelAPI l = Global.getSettings().createLabel(s, Fonts.VICTOR_10);
            labels.add(l);

            LabelAPI l2 = Global.getSettings().createLabel("", "graphics/fonts/orbitron16.fnt");
            LabelNameTiedValueLabel.put(s, l2);
        }
    }

    @Override
    public void onGameLoad(boolean b) {
        super.onGameLoad(b);
        sector = Global.getSector();
        cUI = sector.getCampaignUI();
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
