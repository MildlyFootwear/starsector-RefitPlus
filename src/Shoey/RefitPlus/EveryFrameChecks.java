package Shoey.RefitPlus;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CoreUITabId;

import static Shoey.RefitPlus.MainPlugin.cRR;
import static Shoey.RefitPlus.MainPlugin.cUI;

public class EveryFrameChecks implements EveryFrameScript {
    CoreUITabId last = null;


    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (cUI.getCurrentCoreTab() != last)
        {
            last = cUI.getCurrentCoreTab();
        }
        cRR.FrameChecks();
    }
}
