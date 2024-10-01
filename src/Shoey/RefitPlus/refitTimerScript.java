package Shoey.RefitPlus;

import com.fs.starfarer.api.EveryFrameScript;

import static Shoey.RefitPlus.MainPlugin.refitShipSelecterTimer;

public class refitTimerScript implements EveryFrameScript {
    @Override
    public boolean isDone() {
        return refitShipSelecterTimer > 0.5;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (refitShipSelecterTimer < 0.5)
        {
            refitShipSelecterTimer += amount;
        }
    }
}
