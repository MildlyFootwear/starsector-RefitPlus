package Shoey.RefitPlus;

import Shoey.RefitPlus.Kotlin.Refit;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberStatusAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;
import com.fs.starfarer.campaign.fleet.FleetMember;
import jdk.jfr.internal.LogLevel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.List;

import static Shoey.RefitPlus.MainPlugin.*;


public class CampaignRefitRenderer implements CampaignUIRenderingListener, CampaignInputListener {

    private Logger log = Global.getLogger(this.getClass());

    boolean Cancel = false;

    void FrameChecks()
    {
        if (sector == null || cUI == null || cUI.getCurrentCoreTab() != CoreUITabId.REFIT)
            Cancel = true;
        else
            Cancel = false;

        if (!Cancel)
        {

            FleetMemberAPI current = Refit.INSTANCE.getRefitShip();
            if (current == null)
                log.info("Null ship");
            else
            {
                log.info(current.getShipName());
            }

        }
    }

    @Override
    public void renderInUICoordsBelowUI(ViewportAPI viewport) {


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

        for (InputEventAPI e : events)
        {
            if (e.isConsumed() || e.getEventType() != InputEventType.KEY_DOWN)
                continue;

            int pressedKey = e.getEventValue();
            if (pressedKey == Keyboard.KEY_SPACE) {
                log.setLevel(Level.INFO);
                FrameChecks();
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
