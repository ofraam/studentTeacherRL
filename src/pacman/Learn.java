package pacman;

import static pacman.game.Constants.DELAY;

import java.util.Random;
import java.util.Scanner;

import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.StandardGhosts;
import pacman.game.Constants.MOVE;

/**
 * Run this class to train an agent.
 */
public class Learn {

	public static void main(String[] args) {
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("Training episodes: ");
		int episodes = scanner.nextInt();
		scanner.nextLine();

		// Create the players
		Agent agent = new Agent();
		StandardGhosts ghosts = new StandardGhosts();
		
		// Training sequence
		for (int i=0; i<episodes; i++) {
			System.out.println("Training episode "+(i+1)+"...");
			
			// Start a game
			Random random = new Random();
			Game game = new Game(random.nextLong());
			agent.startEpisode();
			
			// Conduct an episode
			double score = 0;
			while(!game.gameOver()) {
				
				// Take a step
				Game s = game.copy();
				MOVE a = agent.chooseAction(s, s.getPossibleMoves(s.getPacmanCurrentNodeIndex()));
				game.advanceGame(a, ghosts.getMove(game, -1));
				
				// Observe the result
				Game sp = game.copy();
				double r = game.getScore() - score;
				score = game.getScore();
				
				// Learn from the observation
				agent.learn(s, a, sp, r, sp.getPossibleMoves(sp.getPacmanCurrentNodeIndex()));
			}
		}
		
		// See what a trained agent does
		System.out.print("Hit enter to watch: ");
		scanner.nextLine();
		watch(agent);
	}

	/** 
	 * Watch an episode played by this agent.
	 */
	public static void watch(Agent agent) {
		
		// Set exploration rate to zero so it does the best it can
		agent.stopExploring();
		
		Game game = new Game(0);
		StandardGhosts ghosts = new StandardGhosts();
		GameView gv = new GameView(game).showGame();

		while(!game.gameOver()) {
			MOVE a = agent.chooseAction(game, game.getPossibleMoves(game.getPacmanCurrentNodeIndex()));
			game.advanceGame(a, ghosts.getMove(game, -1));
			
			try {
				Thread.sleep(DELAY);
			} catch (Exception e) {}
			
			gv.repaint();
		}
	}
}
