package Shoey.RefitPlus;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CoreUITabId;

import static Shoey.RefitPlus.MainPlugin.*;

public class EveryFrameChecks implements EveryFrameScript {
    CoreUITabId last = null;
    float KotlinTimer = 0;
    int KotlinWaits = 0;

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
        runTime += amount;
        if (cUI.getCurrentCoreTab() != last)
        {
            last = cUI.getCurrentCoreTab();
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
//        if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
//            CampaignRefitListener.RefitInstance.hookCore();
//            cRL.log.debug("\n\n\n\nBeginning dump.\n\n\n\n");
//            mouseX = Global.getSettings().getMouseX();
//            UIDump = "";
//            if (last != CoreUITabId.REFIT)
//                CampaignRefitListener.RefitInstance.dumpDetails(coreUI, "");
//            else
//                CampaignRefitListener.RefitInstance.dumpDetails(refit, "");
//
//            String s;
//
//            if (last == null)
//                s = "null";
//            else
//                s = cUI.getCurrentCoreTab().name();
//
//            int loop = 0;
//            while (UIDump.length() > 1000000*loop)
//            {
//                loop++;
//                int start = 1000000*(loop-1);
//                int end = 1000000*loop;
//                if (end > UIDump.length())
//                    end = UIDump.length();
//                try {
//                    Global.getSettings().writeTextFileToCommon("UIDump " + s + " " + runTime+""+loop, UIDump.substring(start, end));
//                } catch (IOException e) {
//                }
//                if (1000000*loop > end)
//                    break;
//            }
//
//            cRL.log.debug("\n\n\n\nEnding dump.\n\n\n\n");
//
//        }
        cRL.FrameChecks(amount);
        if (KotlinWait)
        {
            KotlinTimer += amount;
            if (KotlinTimer > 0.1)
            {
                KotlinTimer = 0;
                KotlinWaits++;
                if (KotlinWaits > 4)
                {
                    KotlinWaits = 0;
                    KotlinWait = false;
                }
            }
        }
    }
}
