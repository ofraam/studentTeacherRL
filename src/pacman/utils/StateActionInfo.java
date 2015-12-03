package pacman.utils;

import pacman.entries.pacman.FeatureSet;
import pacman.game.Constants.MOVE;

public class StateActionInfo {
	@Override
	public String toString() {
		return act + "\t" + vec + "\t" + qval;
	}

	private MOVE act;
	private FeatureSet vec;
	private double qval;
	
	public StateActionInfo(MOVE a, FeatureSet v, double q){
		setAct(a);
		setVec(v);
		setQval(q);
	}
	

	
	public StateActionInfo(){
		
	}

	
	public MOVE getAct() {
		return act;
	}

	public void setAct(MOVE act) {
		this.act = act;
	}

	public FeatureSet getVec() {
		return vec;
	}

	public void setVec(FeatureSet vec) {
		this.vec = vec;
	}

	public double getQval() {
		return qval;
	}

	public void setQval(double qval) {
		this.qval = qval;
	}
}
