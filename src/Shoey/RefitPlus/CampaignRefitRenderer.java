package Shoey.RefitPlus;

import Shoey.RefitPlus.Kotlin.Refit;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

import static Shoey.RefitPlus.MainPlugin.*;


public class CampaignRefitRenderer implements CampaignUIRenderingListener, CampaignInputListener, CoreUITabListener {

    Logger log = Global.getLogger(this.getClass());
    boolean init;
    FleetMemberAPI FleetMember;
    boolean Cancel = false;
    boolean Wait = false;
    boolean WaitPrinted = false;
    boolean RehookInCheck = false;
    static Refit RefitInstance = new Refit();
    List<LabelAPI> tests = new ArrayList<>();
    String[] fonts = new String[]{Fonts.INSIGNIA_LARGE, Fonts.INSIGNIA_VERY_LARGE, Fonts.ORBITRON_12, Fonts.VICTOR_10, Fonts.ORBITRON_20AA, Fonts.ORBITRON_20AABOLD};
    public UIPanelAPI lastRefit;

    void clearRefitVars()
    {
        refit = null;
        RefitHooked = false;
        FleetMember = null;
    }

    void hookIfNeeded()
    {
        KotlinWait = true;
        RefitInstance.getRefit(refit == null || !RefitHooked);
        while (KotlinWait)
        {
            try {
                log.debug("Kotlin waiting");
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    void InsertOverlay(String s) {

        hookIfNeeded();

        if (RefitHooked && lastRefit != refit && refit != null) {
            try {
                for (LabelAPI l : tests) {
                    refit.removeComponent((UIComponentAPI) l);
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

    void pingRefit(boolean rehook)
    {
        if (rehook || refit == null || !RefitHooked) {
            RefitHooked = false;
            FleetMember = null;
        }
        if (refit == null || !RefitHooked)
        {
            hookIfNeeded();
        }

        if (refit == null)
            return;

        RehookInCheck = false;
        FleetMemberAPI current = RefitInstance.getRefitFleetMember();
        if (current != FleetMember || rehook) {
            FleetMember = current;
            if (current != null) {
                if (needOverlayPlacement)
                    log.debug("Pre-overlay: Updated FM to " + FleetMember.getShipName());
                else
                    log.debug("Post-overlay: Updated FM to " + FleetMember.getShipName());
                needOverlayPlacement = true;
            }
            else
                log.info("Cleared previousFM");
        }

    }

    void FrameChecks() {

        if (sector == null || cUI == null || cUI.getCurrentCoreTab() != CoreUITabId.REFIT || Wait) {
            if (Wait)
            {
                if (!WaitPrinted) {
                    log.debug("Waiting...");
                    WaitPrinted = true;
                }
                RehookInCheck = true;
            }
            Cancel = true;
            RefitHooked = false;
            FleetMember = null;
        } else {
            WaitPrinted = false;
            Cancel = false;
            pingRefit(RehookInCheck);

            if (lastRefit != refit)
            {
                needOverlayPlacement = true;
                log.debug("lastRefit is not refit");
            }

            if (needOverlayPlacement)
                InsertOverlay("");
        }

    }

    @Override
    public void renderInUICoordsBelowUI(ViewportAPI viewport) {

        if (!init) {
            init = true;
            log.setLevel(logLevel);
            LabelAPI ll = null;
            for (String s : fonts) {
                LabelAPI l = Global.getSettings().createLabel("The Game", s);
                l.autoSizeToWidth(l.computeTextWidth(l.getText()));
                if (ll != null)
                    l.getPosition().aboveLeft((UIComponentAPI) ll, 0);
                ll = l;
                tests.add(l);
            }
        }

    }

    @Override
    public void renderInUICoordsAboveUIBelowTooltips(ViewportAPI viewport) {


    }

    @Override
    public void renderInUICoordsAboveUIAndTooltips(ViewportAPI viewport) {

        FrameChecks();

    }

    @Override
    public int getListenerInputPriority() {
        return 100;
    }

    @Override
    public void processCampaignInputPreCore(List<InputEventAPI> events) {

        for (InputEventAPI e : events) {

            if (Cancel)
                return;

            if (e.isConsumed())
                continue;

            if (e.getEventType() == InputEventType.MOUSE_DOWN)
                pingRefit(true);

            int pressedKey = e.getEventValue();
            if (pressedKey == Keyboard.KEY_SPACE) {
                pingRefit(true);
                e.consume();
            }
        }

    }

    @Override
    public void processCampaignInputPreFleetControl(List<InputEventAPI> events) {

    }

    @Override
    public void processCampaignInputPostCore(List<InputEventAPI> events) {

    }

    @Override
    public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
        if (tab != CoreUITabId.REFIT)
            return;
        log.debug("Clearing variables as opening prep");
        refitTimerScript.loopcount = 0;
        clearRefitVars();
    }
}
