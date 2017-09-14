package pacman;

import pacman.entries.pacman.FeatureSet;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by danamir on 28/07/2016.
 */
public class GameState {
    private String state;
    private Collection<String> trajectory;
    private FeatureSet[] features;
    private double Qgap;


    public GameState(String state, FeatureSet[] features, double gap){
        this.state = state;
        this.features = features;
        this.Qgap = gap;
    }
    public GameState(){

    }
    public void setTrajectory(Collection<String> trajectory){
        this.trajectory = trajectory;
    }

    public String getState() {
        return state;
    }

    public FeatureSet[] getFeatures() {
        return features;
    }

    public double getQgap() {
        return Qgap;
    }

    public Collection<String> getTrajectory() {
        return trajectory;
    }
}
