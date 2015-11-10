package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;

import pacman.game.Game;
import pacman.game.Constants.MOVE;

/**
 * SARSA(lambda) with function approximation.
 */
public class SarsaPacMan extends BasicRLPacMan {

	private Random rng = new Random();
	private FeatureSet prototype; // Class to use
	private QFunction Qfunction; // Learned policy

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

	private double EPSILON = 0.05; // Exploration rate
	private double ALPHA = 0.001; // Learning rate
	private double GAMMA = 0.999; // Discount rate
	private double LAMBDA = 0.9; // Backup weighting
	
	private double[] qdiffs; 
	private int qdiffsIndex = 0;
	
	private HashMap<FeatureSet,ArrayList<FeatureSet>> advisedStates;
	
	

	/** Initialize the policy. */
	public SarsaPacMan(FeatureSet proto) {
		prototype = proto;
		Qfunction = new QFunction(prototype);
		qdiffs= new double[100];
		advisedStates = new HashMap<FeatureSet, ArrayList<FeatureSet>>();
	}
	
	public FeatureSet getPrototype()
	{
		return prototype;
	}

	/** Prepare for the first move. */
	public void startEpisode(Game game, boolean testMode) {
		this.testMode = testMode;
		lastScore = 0;
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

	/** Learn if appropriate, and prepare for the next move. */
	public void processStep(Game game) {
		
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
		lastScore = game.getScore();
		delta1 = reward - qvalues[lastActionIndex];
		
		if (!game.gameOver())
			evaluateMoves(game);
		
		// Gradient descent update
		if (!testMode) {
			
			// Right away if game is over
			if (game.gameOver())
			{
				Qfunction.updateWeights(ALPHA*delta1);
				this.maxUpdate();
//				this.maxUpdate();
			}
			
			// Otherwise delayed (for potential advice)
			else
				doUpdate = true;
			if (this.advisedStates.size()>0)
			{
//				System.out.println("------weights before max update--------");
//				System.out.println("bias = "+Qfunction.getBias());
//				double[] currWeights = Qfunction.getWeights();
//				for (int i = 0;i<currWeights.length;i++)
//					System.out.println("weights["+currWeights[i]+"]");
//				System.out.println("------end weights before max update--------");
//				this.maxUpdate();
//				System.out.println("------weights after max update--------");
//				System.out.println("bias = "+Qfunction.getBias());
//				currWeights = Qfunction.getWeights();
//				for (int i = 0;i<currWeights.length;i++)
//					System.out.println("weights["+currWeights[i]+"]");
//				System.out.println("------end weights after max update--------");
			}
		}
		
	}

	/** Compute predictions for moves in this state. */
	private void evaluateMoves(Game game) {

		actions = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());

		features = new FeatureSet[actions.length];
		for (int i=0; i<actions.length; i++)
			features[i] = prototype.extract(game, actions[i]);

		qvalues = new double[actions.length];
		
		qvaluesMap = new HashMap<MOVE, Double>() ;
		
		for (int i=0; i<actions.length; i++)
		{
			double value = Qfunction.evaluate(features[i]);
			qvalues[i] = value;
			qvaluesMap.put(actions[i],value);
		}

		int worstActionIndex = 0;
		bestActionIndex = 0;
		for (int i=0; i<actions.length; i++)
		{
			if (qvalues[i] > qvalues[bestActionIndex])
				bestActionIndex = i;
			if (qvalues[i]<qvalues[worstActionIndex])
				worstActionIndex=i;
		}
		this.updateQdiffs(qvalues[bestActionIndex]-qvalues[worstActionIndex]);

		// Explore or exploit
		if (!testMode && rng.nextDouble() < EPSILON)
			lastActionIndex = rng.nextInt(actions.length);
		else
			lastActionIndex = bestActionIndex;
	}
	
	private void updateQdiffs(double diff)
	{
		qdiffs[qdiffsIndex]=diff;
		qdiffsIndex++;
		if (qdiffsIndex>qdiffs.length-1)
			qdiffsIndex = 0;
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
	
	
	
	public void recordAdvisedState(Game game, MOVE advisedMove)
	{
		ArrayList<FeatureSet> otherActions = new ArrayList<FeatureSet>();
				
		FeatureSet advisedFeatures = this.getFeatures(advisedMove);
		
		if (this.advisedStates.containsKey(advisedFeatures)) //already in history
			return;
		
		for (int i = 0;i<actions.length;i++)
		{
			if (actions[i]!=advisedMove)
			{
				FeatureSet stateActFeatures = this.getFeatures(actions[i]);
				otherActions.add(stateActFeatures);
			}
		}
		if (this.advisedStates.containsKey(advisedFeatures))
			System.out.println("already there");
		else
			this.advisedStates.put(advisedFeatures, otherActions);
	}
	
	public double getAvgQdiff()
	{
		double sum = 0;
		for (int i =0;i<qdiffs.length;i++)
		{
			sum = sum+qdiffs[i];
		}
		return sum/qdiffs.length;
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
}
