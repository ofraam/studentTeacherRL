package pacman.utils;

import java.util.ArrayList;

public class StateInfo {
	private ArrayList<StateActionInfo> stateActionPairs;
	private int time;
	private double varQ;
	private double rangeQ;
	private double reward;
	private StateActionInfo bestAction;
	
	


	public StateInfo(int t)
	{
		time = t;
		stateActionPairs = new ArrayList<StateActionInfo>();
	}
//	
//	public StateInfo(String desc)
//	{
//		String[] info = desc.split("|");
//		time = Integer.parseInt(info[1]);
//		stateActionPairs = new ArrayList<StateActionInfo>();
//		String allSaiInfo = info[0].substring(1, info[0].length()-1);
//		String[] saiInfo = allSaiInfo.split(",");
//		for (int i = 0;i<saiInfo.length;i++)
//		{
//			StateActionInfo sai = new S
//		}
//				
//	}
	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	public StateActionInfo getBestAction() {
		return bestAction;
	}

	public void setBestAction(StateActionInfo bestAction) {
		this.bestAction = bestAction;
	}
	
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	public void addStateActionPair(StateActionInfo sai)
	{
		stateActionPairs.add(sai);
	}

	public ArrayList<StateActionInfo> getStateActionPairs() {
		return stateActionPairs;
	}

	public void setStateActionPairs(ArrayList<StateActionInfo> stateActionPairs) {
		this.stateActionPairs = stateActionPairs;
	}

	@Override
	public String toString() {
		return time+"|" + rangeQ+"|"+varQ+ "|" + reward +"|"+bestAction +"|"+stateActionPairs;
	}
	
	public double getVarQ() {
		return varQ;
	}

	public void setVarQ(double varQ) {
		this.varQ = varQ;
	}

	public double getRangeQ() {
		return rangeQ;
	}

	public void setRangeQ(double rangeQ) {
		this.rangeQ = rangeQ;
	}
	
	
}
