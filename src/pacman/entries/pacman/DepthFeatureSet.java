package pacman.entries.pacman;

import java.util.HashMap;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Features based on object counts a few junctions ahead in the move direction.
 */
public class DepthFeatureSet extends FeatureSet {
	
	// Lazy enum
	private int REGULAR_PILL = 0;
	private int POWER_PILL = 1;
	private int REGULAR_GHOST = 2;
	private int EDIBLE_GHOST = 3;

	private int OBJECTS = 4; // How many
	private int DEPTH = 4; // Of search
	
	private HashMap<Integer,Integer> nodeDepths = new HashMap<Integer,Integer>(); // To avoid double-counting
	
	private int[][] counts = new int[DEPTH][OBJECTS]; // How many at each depth
	private double[] values = new double[OBJECTS*DEPTH]; // Feature values
	
	/** Report how many features there are. */
	public int size() {
		return values.length;
	}

	/** Retrieve a feature value. */
	public double get(int i) {
		return values[i];
	}
	
	/** Extract a feature set for this state-action pair. */
	public FeatureSet extract(Game game, MOVE move) {
		DepthFeatureSet features = new DepthFeatureSet();
		features.setValues(game, move);
		return features;
	}
	
	/** Compute feature values in [0,1). */
	private void setValues(Game game, MOVE move) {
		
		int nodeIndex = game.getPacmanCurrentNodeIndex();
		setCounts(game, nodeIndex, move, 0);
		int v = 0;
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][REGULAR_PILL] / 220.0); // How many at each depth
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][POWER_PILL] / 2.0); // How many at each depth
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][REGULAR_GHOST] / 4.0); // How many at each depth
		
		for (int d=0; d<DEPTH; d++)
			values[v++] = (counts[d][EDIBLE_GHOST] / 4.0); // How many at each depth
		
		if (v != values.length) {
			System.out.println("Feature vector length error: said "+values.length+", got "+v);
			System.exit(0);
		}
	}
	
	/** Count objects a few junctions ahead. */
	private void setCounts(Game game, int nextIndex, MOVE move, int depth) {

		if (depth >= DEPTH)
			return;

		// Count objects in this segment
		while (true) {
			nextIndex = game.getNeighbour(nextIndex, move);
			countObjectsAtIndex(game, nextIndex, depth);

			// Stop at a junction or corner
			if (game.isJunction(nextIndex) || game.getNeighbour(nextIndex, move) == -1)
				break;
		}

		// Continue to next depth
		MOVE[] possibleMoves = game.getPossibleMoves(nextIndex, move);
		for (MOVE nextMove : possibleMoves)
			setCounts(game, nextIndex, nextMove, depth+1);
	}
	
	/** Count objects at this node index. */
	private void countObjectsAtIndex(Game game, int nodeIndex, int depth) {
		
		// Don't double-count nodes
		int oldDepth = -1;
		if (nodeDepths.containsKey(nodeIndex)) {
			oldDepth = nodeDepths.get(nodeIndex);
			if (oldDepth <= depth)
				return;
		}
		nodeDepths.put(nodeIndex, depth);
		
		// Regular pill
		int pillIndex = game.getPillIndex(nodeIndex);
		if (pillIndex >= 0 && game.isPillStillAvailable(pillIndex)) {
			counts[depth][REGULAR_PILL]++;
			if (oldDepth != -1)
				counts[oldDepth][REGULAR_PILL]--;
		}

		// Power pill
		int powerPillIndex = game.getPowerPillIndex(nodeIndex);
		if (powerPillIndex >= 0 && game.isPowerPillStillAvailable(powerPillIndex)) {
			counts[depth][POWER_PILL]++;
			if (oldDepth != -1)
				counts[oldDepth][POWER_PILL]--;
		}

		// Enemy or feast
		for (GHOST ghost : GHOST.values()) {
			int ghostIndex = game.getGhostCurrentNodeIndex(ghost);
			if (ghostIndex == game.getCurrentMaze().lairNodeIndex)
				ghostIndex = game.getGhostInitialNodeIndex();

			if (ghostIndex == nodeIndex) {
				if (game.isGhostEdible(ghost)) {
					counts[depth][EDIBLE_GHOST]++;
					if (oldDepth != -1)
						counts[oldDepth][EDIBLE_GHOST]--;
				}
				else {
					counts[depth][REGULAR_GHOST]++;
					if (oldDepth != -1)
						counts[oldDepth][REGULAR_GHOST]--;
				}
			}
		}
	}
}
