package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEHeuristicRandomExploration {


    @Test
    public void useAppContext() throws Exception {

        MATE.log_acc("Starting Heuristic Random Exploration...");

        MATE mate = new MATE();
        mate.testApp("HeuristicRandom");
    }
}
