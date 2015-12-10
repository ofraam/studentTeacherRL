package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import pacman.game.Game;
import pacman.game.Constants.MOVE;

/**
 * Q(lambda) with function approximation.
 */
public class QPacMan extends BasicRLPacMan {
	
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

	private double EPSILON = 0.05; // Exploration rate
	private double ALPHA = 0.001; // Learning rate
	private double GAMMA = 0.999; // Discount rate
	private double LAMBDA = 0.7; // Backup weighting
	
	private double[] qdiffs; 
	private int qdiffsIndex = 0;
	
	private HashMap<FeatureSet,ArrayList<FeatureSet>> advisedStates;

	/** Initialize the policy. */
	public QPacMan(FeatureSet proto) {
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

	/** Prepare for the next move, and learn if appropriate. */
	public void processStep(Game game) {
		
		// Eligibility traces
		if (lastActionIndex != bestActionIndex)
			Qfunction.clearTraces();
		else
			Qfunction.decayTraces(GAMMA*LAMBDA);
		
		Qfunction.addTraces(features[lastActionIndex]);

		// Q-value correction
		double reward = game.getScore() - lastScore;
		lastScore = game.getScore();
		double delta = reward - qvalues[lastActionIndex];
		
		if (!game.gameOver()) {
			evaluateMoves(game);
			delta += (GAMMA * qvalues[bestActionIndex]);
		}
		
		// Gradient descent update
		if (!testMode)
			Qfunction.updateWeights(ALPHA*delta);
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
		this.advisedStates.put(advisedFeatures, otherActions);
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
		return Qfunction;
	}
}
