package Shoey.RefitPlus;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.ui.LabelAPI;
import org.apache.log4j.Logger;

import static Shoey.RefitPlus.MainPlugin.*;

public class EveryFrameChecks implements EveryFrameScript {
    Logger log = Global.getLogger(this.getClass());
    public static CoreUITabId last = null;
    Thread RLUT = null;
    public static boolean newFrame = false;
    boolean donedid = false;

    int lastCount = 0;
    int frameCount = 0;
    int mouseX = 0;
    float ongoingReflections = 0;
    float ongoingFrames = 0;
    float refitScreenTime = 0;
    float runTime = 0;
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
        if (runTime == 0)
            log.setLevel(logLevel);
        newFrame = true;
        runTime += amount;

        if (cUI.getCurrentCoreTab() != last)
        {
            last = cUI.getCurrentCoreTab();
            RPReflectInstance.hookCore();
            if (last == CoreUITabId.REFIT) {
                for (LabelAPI l : labels) {
                    l.setColor(Global.getSettings().getBrightPlayerColor());
                }
            }
        }

        if (last == CoreUITabId.REFIT)
        {
            if (RLUT == null || !RLUT.isAlive()) {
                RLUT = new Thread(new RefitLabelUpdateThread());
                RLUT.start();
                log.debug("RefitLabelUpdateThread started");
            }
        }


//        if (last == CoreUITabId.REFIT)
//        {
//            if (logLevel == Level.DEBUG) {
//                frameCount++;
//                refitScreenTime += amount;
//                if (reflectionCount > 100) {
//                    ongoingFrames += frameCount;
//                    ongoingReflections += reflectionCount;
//                    frameCount = 0;
//                    reflectionCount = 0;
//                    cRL.log.debug("Approximately "+ongoingReflections+" for "+ongoingFrames+" over "+(refitScreenTime/60)+" minutes");
//                }
//            }
//        }

    }
}
