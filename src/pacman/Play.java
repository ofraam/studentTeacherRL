package pacman;

import static pacman.game.Constants.DELAY;
import java.util.EnumMap;

import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.StandardGhosts;

/**
 * Run this class to play the game.
 */
public class Play {	
	
	public static void main(String[] args) {
		runGameTimed(new HumanController(new KeyBoardInput()), new StandardGhosts(), true);
	}

	public static void runGameTimed(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual) {
		Game game=new Game(0);
		GameView gv=null;

		if(visual)
			gv=new GameView(game).showGame();

		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());

		new Thread(pacManController).start();
		new Thread(ghostController).start();

		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			game.advanceGame(pacManController.getMove(),ghostController.getMove());	   

			if(visual)
				gv.repaint();
		}

		pacManController.terminate();
		ghostController.terminate();
	}
}
