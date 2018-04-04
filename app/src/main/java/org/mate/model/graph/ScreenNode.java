package org.mate.model.graph;

import org.mate.Properties;
import org.mate.state.IScreenState;
import org.mate.ui.Action;

import java.util.Vector;

/**
 * Created by marceloe on 08/12/16.
 */
public class ScreenNode {

    private String id;
    private IScreenState screenState;
    private Vector<EventEdge> eventEdges;
    private boolean searchVisited;
    private boolean explorationVisited;

    private Action triggeredAction;

    public void demo(Action triggeredAction){
        screenState.updatePheromone(triggeredAction);
    }

    public void initPheromone(){
        //TODO: how about selectState.getactions
        Vector<Action> executableActions = screenState.getActions();
        for (Action action:executableActions){
            action.setPheromone(Properties.INITIALIZATION_PHEROMONE);
        }
    }

    public ScreenNode(String id, IScreenState screenState){
        setId(id);
        setScreenState(screenState);
        eventEdges = new Vector<EventEdge>();
        searchVisited = false;

        //init pheromone when creating a new node
        initPheromone();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IScreenState getScreenState() {
        return screenState;
    }

    public void setScreenState(IScreenState screenState) {
        this.screenState = screenState;
    }

    public Vector<EventEdge> getEventEdges() {
        return eventEdges;
    }


    public void addEdge(EventEdge eventEdge){
        eventEdges.add(eventEdge);
    }

    public Vector<ScreenNode> getNeighbors(){
        Vector<ScreenNode> neighbors = new Vector<ScreenNode>();
        for (EventEdge edge: eventEdges){
            neighbors.add(edge.getTarget());
        }
        return neighbors;
    }

    public void setEventEdges(Vector<EventEdge> eventEdges) {
        this.eventEdges = eventEdges;
    }

    public String getPackageName() {
        return screenState.getPackageName();
    }


    public String getActivityName() {
        return screenState.getActivityName();
    }


    public void setSearchVisited(boolean searchVisited) {
        this.searchVisited = searchVisited;
    }

    public boolean isSearchVisited() {
         return this.searchVisited;
    }

    public void setExplorationVisited(boolean explorationVisited) {
        this.explorationVisited = explorationVisited;
    }

    public boolean isExplorationVisited(){
        return explorationVisited;
    }
}
