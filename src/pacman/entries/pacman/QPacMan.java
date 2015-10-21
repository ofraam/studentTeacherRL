package pacman.entries.pacman;

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
	private FeatureSet[] features; // Features for actions in the current state
	
	private int lastScore; // Last known game score
	private int bestActionIndex; // Index of current best action
	private int lastActionIndex; // Index of action actually being taken
	private boolean testMode; // Don't explore or learn or take advice?

	private double EPSILON = 0.05; // Exploration rate
	private double ALPHA = 0.001; // Learning rate
	private double GAMMA = 0.999; // Discount rate
	private double LAMBDA = 0.7; // Backup weighting

	/** Initialize the policy. */
	public QPacMan(FeatureSet proto) {
		prototype = proto;
		Qfunction = new QFunction(prototype);
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
		for (int i=0; i<actions.length; i++)
			qvalues[i] = Qfunction.evaluate(features[i]);

		bestActionIndex = 0;
		for (int i=0; i<actions.length; i++)
			if (qvalues[i] > qvalues[bestActionIndex])
				bestActionIndex = i;
		
		// Explore or exploit
		if (!testMode && rng.nextDouble() < EPSILON)
			lastActionIndex = rng.nextInt(actions.length);
		else
			lastActionIndex = bestActionIndex;
	}
	
	/** Get the current possible moves. */
	public MOVE[] getMoves() {
		return actions;
	}
	
	/** Get the current Q-value array. */
	public double[] getQValues() {
		return qvalues;
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
