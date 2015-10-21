package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Specially crafted features that work well in this domain.
 */
public class CustomFeatureSet extends FeatureSet {

	private int DEPTH = 4; // Of search
	private int FEATURES = 7; // How many
	private double MAX_DISTANCE = 200; // Between nodes
	private double MAX_SCORE = Math.pow(MAX_DISTANCE/4,2); // Of a path

	// Computation storage
	private ArrayList<HashMap<Integer,Double>> junctions;
	private double powerDepth = DEPTH;
	private double pillDepth = DEPTH;

	// Feature values
	public double[] values = new double[FEATURES];

	/** Set up data structures. */
	public CustomFeatureSet() {
		junctions = new ArrayList<HashMap<Integer,Double>>();
		for (int i=0; i<DEPTH; i++)
			junctions.add(new HashMap<Integer,Double>());
	}

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
		CustomFeatureSet features = new CustomFeatureSet();
		features.setValues(game, move);
		return features;
	}

	/** Compute feature values in [0,1). */
	private void setValues(Game game, MOVE move) {
		
		// Things ahead?
		int node = game.getPacmanCurrentNodeIndex();
		exploreJunctions(game, node, move, 0, 0);

		// Safety of junctions?
		double[] safety = new double[DEPTH];
		for (int i=0; i<DEPTH; i++) {
			for (Integer n : junctions.get(i).keySet()) {
				safety[i] = Math.max(safety[i], junctions.get(i).get(n));
				safety[i] = Math.min(safety[0], safety[i]);
			}
		}

		// Feast opportunity?
		double[] startPath = {MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE};
		double[] feastPath = exploreFeasts(game, node, move, 0, 0, startPath);
		double feastScore = score(feastPath);

		// Upcoming feast opportunity?
		double futureFeastScore = 0;
		MOVE[] possibleMoves = game.getPossibleMoves(node);
		for (MOVE possibleMove : possibleMoves) {
			double[] initialPath = {MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE};
			double[] path = exploreEnemies(game, node, possibleMove, 0, 0, initialPath);
			futureFeastScore = Math.max(futureFeastScore, score(path));
		}

		// Features
		int v = 0;
		for (int i=0; i<DEPTH; i++)
			values[v++] = safety[i] / MAX_DISTANCE; // How safe junctions are
		values[v++] = (DEPTH - pillDepth) / DEPTH; // How close a pill is
		values[v++] = feastScore / MAX_SCORE; // How good feasts look
		values[v++] = futureFeastScore / MAX_SCORE / (powerDepth+1); // How good future feasts look
	
		if (v != values.length) {
			System.out.println("Feature vector length error: said "+values.length+", got "+v);
			System.exit(0);
		}
	}

	/** Search up to a few junctions ahead in this direction. */
	private void exploreJunctions(Game game, int node, MOVE move, int depth, double distance) {

		if (depth >= DEPTH)
			return;

		boolean pillInSegment = false;
		boolean powerInSegment = false;

		// Step
		while (true) {
			node = game.getNeighbour(node, move);
			distance++;

			// Stop for an approaching enemy
			for (GHOST ghost : GHOST.values()) {
				if (game.getGhostCurrentNodeIndex(ghost) == node)
					if (move != game.getGhostLastMoveMade(ghost))
						if (!game.isGhostEdible(ghost))
							return;
			}

			// Notice a power pill
			int powerIndex = game.getPowerPillIndex(node);
			if (powerIndex > -1 && game.isPowerPillStillAvailable(powerIndex)) {

				double safety = safety(game, node, distance);
				if (safety <= 0)
					return;

				powerInSegment = true;
				if (depth < powerDepth)
					powerDepth = depth;
			}

			// Notice a regular pill (maybe)
			int pillIndex = game.getPillIndex(node);
			if (pillIndex > -1 && game.isPillStillAvailable(pillIndex))
				pillInSegment = true;

			// Notice a junction
			if (game.isJunction(node)) {

				double safety = safety(game, node, distance);
				if (safety <= 0)
					return;

				if (!junctions.get(depth).containsKey(node) || junctions.get(depth).get(node) < safety)
					junctions.get(depth).put(node, safety);

				// Really notice a regular pill (only in segments without power and with safe exits)
				if (!powerInSegment && pillInSegment && depth < pillDepth)
					pillDepth = depth;
			}

			// Split at a junction
			if (game.isJunction(node)) {

				MOVE[] possibleMoves = game.getPossibleMoves(node, move);
				for (MOVE possibleMove : possibleMoves) {
					exploreJunctions(game, node, possibleMove, depth+1, distance);
				}

				return;
			}

			// Turn at a corner
			else if (game.getNeighbour(node, move) == -1)
				move = game.getPossibleMoves(node, move)[0];
		}
	}

	/** Find the highest-scoring path (ghost : depth) towards enemies in this direction. */
	private double[] exploreEnemies(Game game, int node, MOVE move, int depth, double distance, double[] path) {

		if (depth >= DEPTH-1)
			return path;

		// Step
		while (true) {
			node = game.getNeighbour(node, move);
			distance++;

			// Notice an approaching enemy
			for (GHOST ghost : GHOST.values()) {
				if (game.getGhostCurrentNodeIndex(ghost) == node)
					if (move != game.getGhostLastMoveMade(ghost))
						if (!game.isGhostEdible(ghost))
							if (distance < path[ghost.ordinal()])
								path[ghost.ordinal()] = distance;
			}

			// Split at a junction
			if (game.isJunction(node)) {

				double[] bestPath = path;
				double bestScore = score(path);

				MOVE[] possibleMoves = game.getPossibleMoves(node, move);
				for (MOVE possibleMove : possibleMoves) {

					double[] pathCopy = Arrays.copyOf(path, path.length);
					double[] newPath = exploreEnemies(game, node, possibleMove, depth+1, distance, pathCopy);
					double newScore = score(newPath);

					if (newScore > bestScore) {
						bestPath = newPath;
						bestScore = newScore;
					}
				}

				return bestPath;
			}

			// Turn at a corner
			else if (game.getNeighbour(node, move) == -1)
				move = game.getPossibleMoves(node, move)[0];
		}
	}

	/** Find the path with shortest distances towards feasts in this direction. */
	private double[] exploreFeasts(Game game, int node, MOVE move, int depth, double distance, double[] path) {

		if (depth >= DEPTH+1)
			return path;

		// Step
		while (true) {
			node = game.getNeighbour(node, move);
			distance++;

			// Stop for a power pill
			int powerIndex = game.getPowerPillIndex(node);
			if (powerIndex > -1 && game.isPowerPillStillAvailable(powerIndex))
				return path;

			for (GHOST ghost : GHOST.values()) {
				if (game.getGhostCurrentNodeIndex(ghost) == node) {

					// Stop for an approaching enemy
					if (!game.isGhostEdible(ghost)) {
						if (move != game.getGhostLastMoveMade(ghost))
							return path;
					}

					else {
						
						// Stop for an unsafe feast
						double safety = safety(game, node, distance);
						if (safety <= 0)
							return path;

						// Otherwise notice it
						if (distance < path[ghost.ordinal()])
							path[ghost.ordinal()] = distance;
					}
				}
			}

			// Split at a junction
			if (game.isJunction(node)) {

				double[] bestPath = path;
				double bestScore = score(path);

				MOVE[] possibleMoves = game.getPossibleMoves(node, move);
				for (MOVE possibleMove : possibleMoves) {

					double[] pathCopy = Arrays.copyOf(path, path.length);
					double[] newPath = exploreFeasts(game, node, possibleMove, depth+1, distance, pathCopy);
					double newScore = score(newPath);

					if (newScore > bestScore) {
						bestPath = newPath;
						bestScore = newScore;
					}
				}

				return bestPath;
			}

			// Turn at a corner
			else if (game.getNeighbour(node, move) == -1)
				move = game.getPossibleMoves(node, move)[0];
		}
	}

	/** Compute the score of a ghost path. Closer ghosts mean higher scores. */
	private double score(double[] path) {
		double score = 0;
		for (int i=0; i<path.length; i++)	
			score += Math.pow((MAX_DISTANCE - path[i])/4, 2);
		return score / path.length;
	}

	/** Compute the safety of a target node. Further ghosts means safer nodes. */
	private double safety(Game game, Integer node, double myDistance) {
		double[] enemyDistances = enemyNodeDistances(game, node);
		Arrays.sort(enemyDistances);
		return enemyDistances[0] - myDistance - Constants.EAT_DISTANCE;
	}

	/** Compute relevant enemy distances to a nearby node. */
	private double[] enemyNodeDistances(Game game, int node) {

		double[] distances = new double[GHOST.values().length];
		for (int i=0; i<distances.length; i++)
			distances[i] = MAX_DISTANCE;

		for (GHOST ghost : GHOST.values()) {
			if (!game.isGhostEdible(ghost)) {
				int ghostNode = game.getGhostCurrentNodeIndex(ghost);

				// Ignore ghosts in the lair
				if (ghostNode != game.getCurrentMaze().lairNodeIndex) {

					// Ignore ghosts I could reach before the target (close followers)
					int myNode = game.getPacmanCurrentNodeIndex();
					if (game.getDistance(myNode, node, DM.PATH) < game.getDistance(myNode, ghostNode, DM.PATH)) {

						MOVE ghostMove = game.getGhostLastMoveMade(ghost);
						distances[ghost.ordinal()] = game.getDistance(ghostNode, node, ghostMove, DM.PATH);
					}
				}
			}
		}

		return distances;
	}
}
