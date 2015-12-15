package pacman.entries.pacman;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;

import pacman.game.Game;
import pacman.utils.DataFile;
import pacman.utils.StateActionInfo;
import pacman.utils.StateInfo;
import pacman.utils.Stats;
import pacman.game.Constants.MOVE;

/**
 * SARSA(lambda) with function approximation.
 */
public class SarsaPacMan extends BasicRLPacMan {

	private Random rng = new Random();
	private FeatureSet prototype; // Class to use
	public QFunction Qfunction; // Learned policy

	private MOVE[] actions; // Actions possible in the current state
	private double[] qvalues; // Q-values for actions in the current state
	private Map<MOVE, Double> qvaluesMap; // mapping from actions to q-values
	private FeatureSet[] features; // Features for actions in the current state

	private int lastScore; // Last known game score
	private int bestActionIndex; // Index of current best action
	private int lastActionIndex; // Index of action actually being taken
	private boolean testMode; // Don't explore or learn or take advice?
	private boolean doUpdate; // Perform a delayed gradient-descent update?
	private double delta1; // First part of delayed update: r-Q(s,a)
	private double delta2; // Second part of delayed update: yQ(s',a')

	private double EPSILON = 0.05; // Exploration rate 0.05superUn2!
	private double ALPHA = 0.001; // Learning rate
	private double GAMMA = 0.999; // Discount rate
	private double LAMBDA = 0.9; // Backup weighting
	
	private double[] qdiffs; 
	private int qdiffsIndex = 0;
	
	private int time;
	private boolean printStates = false;
	private StateInfo currStateInfo;
	private String filename = "myData/stateInfoTeacherMultiple.txt";
	private BufferedWriter  file;
	private HashMap<FeatureSet,ArrayList<FeatureSet>> advisedStates;
	
	private String maxUpdateTiming = "never"; //epidsodeEnd = at end of episode, batch = end of episode, but batch update,never = never, atState = when state is encountered

	/** Initialize the policy. */
	public SarsaPacMan(FeatureSet proto) {
		prototype = proto;
		Qfunction = new QFunction(prototype);
		qdiffs= new double[2000];
		advisedStates = new HashMap<FeatureSet, ArrayList<FeatureSet>>();
		if (printStates)
		{
			try {
				File f = new File(filename);
				file = new BufferedWriter(new FileWriter(f));
				if (!f.exists()) {
					f.createNewFile();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	public FeatureSet getPrototype()
	{
		return prototype;
	}

	/** Prepare for the first move. */
	public void startEpisode(Game game, boolean testMode) {
		if (printStates)
		{
			try {
				file.write("-------------new episode----------------\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.testMode = testMode;
		lastScore = 0;
		time = 0;
		Qfunction.clearTraces();
//		if (testMode)
//		{
//			System.out.println("bias = "+Qfunction.getBias());
//			double[] currWeights = Qfunction.getWeights();
//			for (int i = 0;i<currWeights.length;i++)
//				System.out.println("weights["+currWeights[i]+"]");
//		}
		doUpdate = false;
		delta1 = 0;
		delta2 = 0;
		advisedStates = new HashMap<FeatureSet, ArrayList<FeatureSet>>();
		currStateInfo = new StateInfo(time);
		evaluateMoves(game);
	}

	/** Choose a move. */
	public MOVE getMove(Game game, long timeDue) {
		return actions[lastActionIndex];
	}
	
	/** Override the move choice. */
	public void setMove(MOVE move) {
		lastActionIndex = -1;
		for (int i=0; i<actions.length; i++)
			if (actions[i] == move)
				lastActionIndex = i;
	}

	private void checkMaxUpdateState(Game game)
	{
		for (int i = 0;i<actions.length;i++)
		{
			FeatureSet currFeatures = prototype.extract(game, actions[i]);
			if (this.advisedStates.containsKey(currFeatures))
			{
				FeatureSet maxFeatures = this.getMaxQfeatures();
				Qfunction.maxUpdate(currFeatures, maxFeatures, ALPHA);
			}
		}
	}
	
	private FeatureSet getMaxQfeatures()
	{
		double maxQ = -Integer.MAX_VALUE;
		FeatureSet maxQfeatures = null;
		for (int i = 0;i<actions.length;i++)
		{
			FeatureSet stateActFeatures = this.getFeatures(actions[i]);
			double currQ = Qfunction.evaluate(stateActFeatures);
			if (currQ>maxQ)
			{
				maxQ = currQ;
				maxQfeatures = stateActFeatures;
			}
		}
		return maxQfeatures;
	}
	
	/** Learn if appropriate, and prepare for the next move. */
	public void processStep(Game game) {
		currStateInfo = new StateInfo(time);
		
		// Do a delayed gradient-descent update
		if (doUpdate) {
			delta2 = (GAMMA * qvalues[lastActionIndex]);
			Qfunction.updateWeights(ALPHA*(delta1+delta2));
//			this.maxUpdate();
	
		}
		
		// Eligibility traces
		Qfunction.decayTraces(GAMMA*LAMBDA);
		Qfunction.addTraces(features[lastActionIndex]);
		

		
		// Q-value correction
		double reward = game.getScore() - lastScore;
		currStateInfo.setReward(reward);
		lastScore = game.getScore();
		delta1 = reward - qvalues[lastActionIndex];
		
		if (!game.gameOver())
		{
			evaluateMoves(game);
			if (this.maxUpdateTiming=="atState" & !testMode)
			{
				this.checkMaxUpdateState(game);
			}
		}
		
		

		
		// Gradient descent update
		if (!testMode) {
			
			// Right away if game is over
			if (game.gameOver())
			{

				if (printStates)
				{
					try {
						file.flush();
						file.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Qfunction.updateWeights(ALPHA*delta1);
//				if (this.maxUpdateTiming=="atState")
//				{
//					this.checkMaxUpdateState(game);
//				}
				if (this.maxUpdateTiming=="epidsodeEnd")
					this.maxUpdate();
				if (this.maxUpdateTiming=="batch" & this.advisedStates.size()>0)
					this.maxUpdateBatch();
			}
			
			// Otherwise delayed (for potential advice)
			else
				doUpdate = true;

		}
		if (printStates)
		{
			try {
				file.write(currStateInfo.toString());
				file.write("\n");
				file.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		time++;
		
	}

	/** Compute predictions for moves in this state. */
	private void evaluateMoves(Game game) {

		actions = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
//		if (actions.length<4)
//		{
//			System.out.println("prob");
//		}
		features = new FeatureSet[actions.length];
		for (int i=0; i<actions.length; i++)
			features[i] = prototype.extract(game, actions[i]);

		qvalues = new double[actions.length];
		
		qvaluesMap = new HashMap<MOVE, Double>() ;
		
		double highestQ=-Double.MAX_VALUE;
		for (int i=0; i<actions.length; i++)
		{
			double value = Qfunction.evaluate(features[i]);
			qvalues[i] = value;
			qvaluesMap.put(actions[i],value);
			StateActionInfo sai = new StateActionInfo(actions[i],features[i],value);
			currStateInfo.addStateActionPair(sai);
			if (value>highestQ)
			{
				highestQ=value;
				currStateInfo.setBestAction(sai);
			}
		}
		currStateInfo.setRangeQ(Stats.range(qvalues));
		currStateInfo.setVarQ(Stats.variance(qvalues));

		int worstActionIndex = 0;
		bestActionIndex = 0;
		for (int i=0; i<actions.length; i++)
		{
			if (qvalues[i] > qvalues[bestActionIndex])
				bestActionIndex = i;
			if (qvalues[i]<qvalues[worstActionIndex])
				worstActionIndex=i;
		}
		if (!testMode)
			this.updateQdiffs(qvalues[bestActionIndex]-qvalues[worstActionIndex]);

		// Explore or exploit
		if (!testMode && rng.nextDouble() < EPSILON)
			lastActionIndex = rng.nextInt(actions.length);
		else
			lastActionIndex = bestActionIndex;
	}
	

	
	private void maxUpdate()
	{
		List<FeatureSet> keys = new ArrayList(this.advisedStates.keySet());
		Collections.shuffle(keys);
		for (FeatureSet advisedFeature:keys){
			ArrayList<FeatureSet> others = this.advisedStates.get(advisedFeature);
			int maxQindex = 0;
			double maxQvalue = -Integer.MAX_VALUE;
			for (int i = 0;i<others.size();i++)
			{
				double currQ = Qfunction.evaluate(others.get(i));
				if (currQ>maxQvalue)
				{
					maxQindex=i;
					maxQvalue = currQ;
				}
			}
			double advisedActQ = Qfunction.evaluate(advisedFeature);
			if (advisedActQ<maxQvalue)//do gradient descent update
			{
				Qfunction.maxUpdate(advisedFeature, others.get(maxQindex), ALPHA);
			}
			else
			{
				this.advisedStates.remove(advisedFeature);
			}
		}
	}
	
	private void maxUpdateBatch()
	{
		List<FeatureSet> keys = new ArrayList(this.advisedStates.keySet());
		double[] advisedAvg =  new double [prototype.size()];
		double[] maxAvg =  new double [prototype.size()];
		double counter = 0;
		for (FeatureSet advisedFeature:keys){
			ArrayList<FeatureSet> others = this.advisedStates.get(advisedFeature);
			int maxQindex = 0;
			double maxQvalue = -Integer.MAX_VALUE;
			for (int i = 0;i<others.size();i++)
			{
				double currQ = Qfunction.evaluate(others.get(i));
				if (currQ>maxQvalue)
				{
					maxQindex=i;
					maxQvalue = currQ;
				}
			}
			double advisedActQ = Qfunction.evaluate(advisedFeature);
			if (advisedActQ<maxQvalue)//do gradient descent update
			{
				FeatureSet other = others.get(maxQindex);
				counter++;
				for (int f=0;f<advisedAvg.length;f++)
				{
					advisedAvg[f]+=advisedFeature.get(f);
					maxAvg[f]+=other.get(f);
				}
				
			}
			else
			{
				this.advisedStates.remove(advisedFeature);
			}
		}
		for (int f=0;f<advisedAvg.length;f++)
		{
			advisedAvg[f]=advisedAvg[f]/counter;
			maxAvg[f]=maxAvg[f]/counter;
		}
		Qfunction.maxUpdate(advisedAvg,maxAvg, ALPHA);
	}
	
	
	
	public void recordAdvisedState(Game game, MOVE advisedMove)
	{
		ArrayList<FeatureSet> otherActions = new ArrayList<FeatureSet>();
				
		FeatureSet advisedFeatures = this.getFeatures(advisedMove);
		
		if (this.advisedStates.containsKey(advisedFeatures)) //already in history
			return;
		
		double maxQ = -Integer.MAX_VALUE;
		FeatureSet maxQfeatures = null;
		for (int i = 0;i<actions.length;i++)
		{
			if (actions[i]!=advisedMove)
			{
				FeatureSet stateActFeatures = this.getFeatures(actions[i]);
				otherActions.add(stateActFeatures);
				double currQ = Qfunction.evaluate(stateActFeatures);
				if (currQ>maxQ)
				{
					maxQ = currQ;
					maxQfeatures = stateActFeatures;
				}
			}
		}
		if (!this.advisedStates.containsKey(advisedFeatures))
			this.advisedStates.put(advisedFeatures, otherActions);
		if (this.maxUpdateTiming=="atState")
		{
			Qfunction.maxUpdate(advisedFeatures, maxQfeatures, ALPHA);
		}
	}
	
	public double getAvgQdiff()
	{

		double sum = 0;
		int runUntil = Math.min(qdiffsIndex, qdiffs.length);
		for (int i =0;i<runUntil;i++)
		{
			sum = sum+qdiffs[i];
		}
		return sum/qdiffsIndex;
	}	
	
	private void updateQdiffs(double diff)
	{
		if (qdiffsIndex==qdiffs.length)
		{
			int i = 0;
			i++;
		}
		qdiffs[qdiffsIndex % qdiffs.length]=diff;
		qdiffsIndex++;
//		if (qdiffsIndex>qdiffs.length-1)
//			qdiffsIndex = 0;
	}
	
	public double getNthQvalue(int n)
	{
		Arrays.sort(qdiffs);
		return qdiffs[n];
	}
	
	/** Get the current possible moves. */
	public MOVE[] getMoves() {
		return actions;
	}
	
	/** Get the current Q-value array. */
	public double[] getQValues() {
		return qvalues;
	}
	
	/** Get the current Q-value map. */
	public Map<MOVE,Double> getQValuesDict() {
		return qvaluesMap;
	}
	
		
	/** Get the current features for an action. */
	public FeatureSet getFeatures(MOVE move) {
		int actionIndex = -1;
		for (int i=0; i<actions.length; i++)
			if (actions[i] == move)
				actionIndex = i;
		return features[actionIndex];
	}
	
	/** Save the current policy to a file. */
	public void savePolicy(String filename) {
		Qfunction.save(filename);
	}

	/** Return to a policy from a file. */
	public void loadPolicy(String filename) {
		Qfunction = new QFunction(prototype, filename);
	}

	@Override
	public void saveStates(String filename, double size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadVisitedState(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public QFunction getQfunc() {
		// TODO Auto-generated method stub
		return Qfunction;
	}
}
