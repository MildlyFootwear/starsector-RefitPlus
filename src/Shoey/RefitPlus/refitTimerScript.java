package Shoey.RefitPlus;

import com.fs.starfarer.api.EveryFrameScript;

import static Shoey.RefitPlus.MainPlugin.cRR;

public class refitTimerScript implements EveryFrameScript {
    boolean fin = false;
    float refitShipSelecterTimer = 0;
    int frame = 0;
    @Override
    public boolean isDone() {
        return !cRR.Wait;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (frame < 10) {
            if (frame == 0)
                cRR.FrameChecks();
            frame++;
        }
        else {
            if (refitShipSelecterTimer < 0.1) {
                refitShipSelecterTimer += amount;
            } else {
                cRR.Wait = false;
                fin = true;
            }
        }
    }
}
