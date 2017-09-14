package pacman;

import java.util.Comparator;

/**
 * Created by danamir on 28/07/2016.
 */
public class GapComparator implements Comparator<GameState> {
    @Override
    public int compare(GameState o1, GameState o2) {
        return Double.compare(o1.getQgap(), o2.getQgap());
    }
}
