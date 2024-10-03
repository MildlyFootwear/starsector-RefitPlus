package Shoey.RefitPlus;

import com.fs.starfarer.api.EveryFrameScript;

import static Shoey.RefitPlus.MainPlugin.cRR;

public class refitTimerScript implements EveryFrameScript {
    boolean fin = false;
    float refitShipSelecterTimer = 0;
    public static int loopcount = 0;
    @Override
    public boolean isDone() {
        return fin;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (loopcount == 5)
        {
            cRR.log.debug("Stopping monitor loop.");
            fin = true;
            loopcount = 0;
            return;
        }
        if (refitShipSelecterTimer < 0.1) {
            refitShipSelecterTimer += amount;
        } else {
            loopcount++;
            cRR.pingRefit(true);
            fin = true;
        }
    }
}
