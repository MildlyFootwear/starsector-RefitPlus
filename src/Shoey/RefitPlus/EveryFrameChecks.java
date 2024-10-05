package Shoey.RefitPlus;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.ui.LabelAPI;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

import static Shoey.RefitPlus.MainPlugin.*;

public class EveryFrameChecks implements EveryFrameScript {
    Logger log = Global.getLogger(this.getClass());
    public static CoreUITabId last = null;
    Thread RLUT = null;
    public static boolean newFrame = false;


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
            RPReflectInstance.unhook();
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
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {

            String s;

            if (last == null)
                s = "null";
            else
                s = cUI.getCurrentCoreTab().name();
            String UIDump;
            if (last != CoreUITabId.REFIT)
                UIDump = dump.dump(coreUI);
            else
                UIDump = dump.dump(refit);

            log.debug(UIDump.length());
            if (UIDump.length() > 0) {
                int loop = 0;
                while (true) {
                    loop++;
                    int start = 1000000 * (loop - 1);
                    int end = 1000000 * loop;
                    if (end > UIDump.length()) {
                        end = UIDump.length();
                    }
                    try {
                        Global.getSettings().writeTextFileToCommon("UIDump " + s + " " + runTime + " " + loop, UIDump.substring(start, end));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                    if (1000000 * loop > end) {
                        break;
                    }
                }
            }
        }
    }
}
