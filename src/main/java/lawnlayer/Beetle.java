package lawnlayer;

/**
 * Represents an enemy agent of type "beetle".
 */
public class Beetle extends Enemy {
    
    /**
     * Creates a new beetle enemy with specified (x,y) coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Beetle(int x, int y) {
        super(x,y);
    }
    
    /**
     * Defines the attacks this beetle performs.
     * Beetles extends {@link lawnlayer.Enemy} attack behaviours. In addition, it removes
     * the filled grass tiles upon collision. See {@link lawnlayer.Enemy#attack(App, Player, TileObject)}.
     */
    @Override
    public void attack(App gameboard, Player player, TileObject agentTile) {
        super.attack(gameboard, gameboard.getPlayer(), agentTile);
        if (agentTile.isGrass()) {
            agentTile.setSoil();
        }
    }

}
