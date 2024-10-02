package Shoey.RefitPlus;

import Shoey.RefitPlus.Kotlin.Refit;
import com.fs.starfarer.api.EveryFrameScript;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

import static Shoey.RefitPlus.MainPlugin.*;


public class CampaignRefitRenderer implements CampaignUIRenderingListener, CampaignInputListener, CoreUITabListener {

    private Logger log = Global.getLogger(this.getClass());
    boolean init;
    FleetMemberAPI FleetMember;
    boolean Cancel = false;
    boolean needOverlayPlacement = true;
    Refit RefitInstance = new Refit();
    List<LabelAPI> tests = new ArrayList<>();
    String[] fonts = new String[]{Fonts.INSIGNIA_LARGE, Fonts.INSIGNIA_VERY_LARGE, Fonts.ORBITRON_12, Fonts.VICTOR_10, Fonts.ORBITRON_20AA, Fonts.ORBITRON_20AABOLD};
    UIPanelAPI lastRefit;
    float timer = 0;
    void UpdateOverlay() {

        if (RefitHooked) {
            try {
                for (LabelAPI l : tests) {
                    refit.removeComponent((UIComponentAPI) l);
                    refit.addComponent((UIComponentAPI) l);
                }
                log.info("Added labels");
                lastRefit = refit;
                needOverlayPlacement = false;
            } catch (Exception e) {
                needOverlayPlacement = true;
            }
        } else {
            needOverlayPlacement = true;
            log.error("Refit not hooked.");
        }
    }

    void pingRefit(boolean rehook)
    {
        FleetMemberAPI current = RefitInstance.getRefitShip(rehook);
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

        if (sector == null || cUI == null || cUI.getCurrentCoreTab() != CoreUITabId.REFIT) {
            Cancel = true;
            RefitHooked = false;
            FleetMember = null;
        }
        else {
            Cancel = false;
            pingRefit(false);

            if (lastRefit != refit)
            {
                needOverlayPlacement = true;
                log.debug("lastRefit is not refit");
            }

            if (needOverlayPlacement)
                UpdateOverlay();
        }

    }

    @Override
    public void renderInUICoordsBelowUI(ViewportAPI viewport) {

        if (!init) {
            init = true;
            log.setLevel(logLevel);
            float x = 0;
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

        FrameChecks();


    }

    @Override
    public void renderInUICoordsAboveUIBelowTooltips(ViewportAPI viewport) {

        if (Cancel)
            return;


    }

    @Override
    public void renderInUICoordsAboveUIAndTooltips(ViewportAPI viewport) {

        if (Cancel)
            return;

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
        log.debug("Clearing variables as opening prep");
        Cancel = true;
        RefitHooked = false;
        FleetMember = null;
    }
}
