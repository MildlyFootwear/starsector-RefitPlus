package Shoey.RefitPlus;

import Shoey.RefitPlus.Kotlin.Refit;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

import static Shoey.RefitPlus.MainPlugin.*;


public class CampaignRefitRenderer implements CampaignUIRenderingListener, CampaignInputListener {

    private Logger log = Global.getLogger(this.getClass());
    boolean init;
    FleetMemberAPI FleetMember;
    boolean Cancel = false;
    Refit RefitInstance = new Refit();
    List<LabelAPI> tests = new ArrayList<>();
    String[] fonts = new String[]{Fonts.INSIGNIA_LARGE, Fonts.INSIGNIA_VERY_LARGE, Fonts.ORBITRON_12, Fonts.VICTOR_10, Fonts.ORBITRON_20AA, Fonts.ORBITRON_20AABOLD};


    void UpdateOverlay() {
        if (RefitHooked) {
            for (LabelAPI l : tests) {
                try {
                    refit.removeComponent((UIComponentAPI) l);
                    refit.addComponent((UIComponentAPI) l);
                } catch (Exception e) {
                }
            }

        } else {
            log.error("Refit not hooked.");
        }
        //test.getPosition().setLocation(Global.getSettings().getScreenWidth()/2, Global.getSettings().getScreenHeight()/2);
    }

    void pingRefit(boolean print)
    {
        FleetMemberAPI current = RefitInstance.getRefitShip(print);
        if (current != FleetMember) {
            FleetMember = current;
            if (current != null)
                log.info("Updated FM to " + FleetMember.getShipName());
            else
                log.info("Cleared previousFM");

            if (FleetMember != null) {
                UpdateOverlay();
            }
        }
    }

    void FrameChecks() {

        Cancel = sector == null || cUI == null || cUI.getCurrentCoreTab() != CoreUITabId.REFIT;

        if (!Cancel) {

            pingRefit(false);

        } else
            RefitHooked = false;

    }

    @Override
    public void renderInUICoordsBelowUI(ViewportAPI viewport) {

        if (!init) {
            init = true;
            log.setLevel(Level.INFO);
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

        if (Cancel)
            return;


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
        return 0;
    }

    @Override
    public void processCampaignInputPreCore(List<InputEventAPI> events) {

        for (InputEventAPI e : events) {
            if (e.isConsumed())
                continue;

            if (e.getEventType() == InputEventType.MOUSE_DOWN)
                RefitHooked = false;

            int pressedKey = e.getEventValue();
            if (pressedKey == Keyboard.KEY_SPACE) {
                pingRefit(true);
            }
        }

    }

    @Override
    public void processCampaignInputPreFleetControl(List<InputEventAPI> events) {

    }

    @Override
    public void processCampaignInputPostCore(List<InputEventAPI> events) {

    }
}
