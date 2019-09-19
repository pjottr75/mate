package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityInfoChecker;
import org.mate.accessibility.check.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.check.MultipleContentDescCheck;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.Date;
import java.util.List;

import static org.mate.MATE.device;

/**
 * Created by geyan on 11/06/2017.
 */

public class ManualExploration {


    public ManualExploration(){

    }

    public void startManualExploration(long runningTime) {

        long currentTime = new Date().getTime();

        MATE.log("MATE TIMEOUT: " + MATE.TIME_OUT);
        Action manualAction = new WidgetAction(ActionType.MANUAL_ACTION);
        while (currentTime - runningTime <= MATE.TIME_OUT){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

            for (Widget w: state.getWidgets()){
                MATE.log_acc(w.getClazz()+"-"+w.getId()+"-"+w.getText()+"-"+w.getBounds());
            }


            boolean foundNewState  = MATE.uiAbstractionLayer.checkIfNewState(state);
            if (foundNewState){

                MATE.uiAbstractionLayer.executeAction(manualAction);
                state = MATE.uiAbstractionLayer.getCurrentScreenState();
                EnvironmentManager.screenShot(state.getPackageName(),state.getId());


                MATE.logactivity(state.getActivityName());

                AccessibilityInfoChecker accChecker = new AccessibilityInfoChecker();
                AccessibilitySummaryResults.currentActivityName=state.getActivityName();
                AccessibilitySummaryResults.currentPackageName=state.getPackageName();
                accChecker.runAccessibilityTests(state);
                //MATE.log_acc("CHECK CONTRAST");

                MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(state);
                ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck(state.getPackageName(),state.getActivityName(),state.getId(),device
                        .getDisplayWidth(),device.getDisplayHeight());
                for (Widget widget: state.getWidgets()) {

                    boolean contrastRatioOK = contrastChecker.check(widget);
                    //MATE.log("Check contrast of "+widget.getId() + ": " + contrastChecker.contratio);

                    if (!contrastRatioOK)
                        AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,String.valueOf(contrastChecker.contratio));

                    boolean multDescOK = multDescChecker.check(widget);
                    if (!multDescOK)
                        AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

                }

            }
            currentTime = new Date().getTime();
        }
    }

}
