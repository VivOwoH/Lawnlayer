package lawnlayer;

import java.util.HashMap;

/**
 * Represents a game object that actively performs some actions.
 */
public class Agent extends GameObject {

    protected final int PPF = 2; 
    private final String[] KEYS = {
        "moveUp",
        "moveRight",
        "moveDown",
        "moveLeft"};
    protected HashMap<Integer,String> movementIdx = new HashMap<Integer,String>();
    protected HashMap<String,Boolean> movements = new HashMap<String,Boolean>();

    /**
     * Creates a new agent with specified (x,y) coordinates.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Agent(int x, int y) {
        super(x,y);
        for (String value : KEYS)
            movementIdx.put(movementIdx.size(), value);
        for (String key : KEYS)
            movements.put(key, false);
    }

    /**
     * Updates this agent's movement and action. Called every frame.
     */
    public void tick() {
        if (movements.get("moveUp"))
            this.setY(this.getY() - PPF);
        if (movements.get("moveRight"))
            this.setX(this.getX() + PPF);
        if (movements.get("moveDown"))
            this.setY(this.getY() + PPF);
        if (movements.get("moveLeft"))
            this.setX(this.getX() - PPF);
    }

    /**
     * Keeps the agent within the game area if it moves beyond boundaries.
     * @param x current x-coordinate of the agent
     * @param y current y-coordinate of the agent
     */
    public void snapBoundary(int x, int y) {
        x = Math.max(x, App.SPRITESIZE()/2);
        x = Math.min(x, App.SPRITESIZE() * App.GRID_COL() 
                                    - App.SPRITESIZE()/2);
        y = Math.max(y, App.TOPBAR() + App.SPRITESIZE()/2);
        y = Math.min(y, App.TOPBAR() + App.SPRITESIZE() * App.GRID_ROW() 
                                    - App.SPRITESIZE()/2);

        this.setX(x);
        this.setY(y);
    }
    
    /**
     * Gets the tile where the agent is currently located in.
     * @param allTileObjects all tile objects on screen
     * @return               the agent's tile
     */
    public TileObject getTile(TileObject[] allTileObjects) {
        TileObject agentTile = null;
        for (TileObject tile : allTileObjects) {
            if (this.getX() >= tile.getX()-App.SPRITESIZE()/2 && this.getX() < tile.getX()+App.SPRITESIZE()/2 && 
                this.getY() >= tile.getY()-App.SPRITESIZE()/2 && this.getY() < tile.getY()+App.SPRITESIZE()/2) {
                    agentTile = tile;
            }               
        }
        return agentTile;
    }


    /**
     * Get surrounding 8 tiles of the agent.
     * <p> 1 2 3 
     * <p> 4 o 5
     * <p> 6 7 8
     * <p> At the 4 walls and 4 corners of the tilemap, surrounding tiles on the 
     * same side cannot be obtained (e.g.at right wall, tile 3,5,8 cannot be obtained).
     * @param map       the tilemap of this game
     * @param agentTile the agent's tile
     * @return          the agent's surrounding 8 tiles in a <code>HashMap</code>
     */
    public HashMap<Integer,TileObject> getSurroundingTiles(TileObject[][] map, TileObject agentTile) {       
        int idxX = agentTile.getRow();
        int idxY = agentTile.getCol();
        HashMap<Integer,TileObject> surroundingTiles = new HashMap<Integer,TileObject>();
        
        // left wall
        if (idxY == 0) {
            surroundingTiles.put(5, map[idxX][idxY+1]);    // 5
            if (idxX != 0) { // not top-left corner
                surroundingTiles.put(2, map[idxX-1][idxY]);    // 2
                surroundingTiles.put(3, map[idxX-1][idxY+1]);  // 3
            } 
            if (idxX != map.length-1) { // not bottom-left corner
                surroundingTiles.put(7, map[idxX+1][idxY]);    // 7
                surroundingTiles.put(8, map[idxX+1][idxY+1]);   // 8
            }
        } 
        // right wall
        else if (idxY == map[0].length-1){
            surroundingTiles.put(4, map[idxX][idxY-1]);    // 4
            if (idxX != 0) { // not top-right corner
                surroundingTiles.put(1, map[idxX-1][idxY-1]);  // 1
                surroundingTiles.put(2, map[idxX-1][idxY]);    // 2
            }
            if (idxX != map.length-1) { // not bottom-right corner
                surroundingTiles.put(6, map[idxX+1][idxY-1]);  // 6
                surroundingTiles.put(7, map[idxX+1][idxY]);    // 7
            }
        }
        // top wall 
        else if (idxX == 0) {
            surroundingTiles.put(7, map[idxX+1][idxY]);    // 7
            if (idxY != 0) { // not top-left corner
                surroundingTiles.put(4, map[idxX][idxY-1]);    // 4
                surroundingTiles.put(6, map[idxX+1][idxY-1]);  // 6
            }
            if (idxY != map[0].length-1) { // not top-right corner
                surroundingTiles.put(5, map[idxX][idxY+1]);    // 5
                surroundingTiles.put(8, map[idxX+1][idxY+1]);  // 8
            }
        }
        // bottom wall 
        else if (idxX == map.length-1) {
            surroundingTiles.put(2, map[idxX-1][idxY]);    // 2
            if (idxY != 0) { // not bottom-left corner
                surroundingTiles.put(1, map[idxX-1][idxY-1]);  // 1
                surroundingTiles.put(4, map[idxX][idxY-1]);    // 4
            }
            if (idxY != map[0].length-1) { // not bottom-right corner
                surroundingTiles.put(3, map[idxX-1][idxY+1]);  // 3
                surroundingTiles.put(5, map[idxX][idxY+1]);    // 5
            }
        } else {
            surroundingTiles.put(1, map[idxX-1][idxY-1]);  // 1
            surroundingTiles.put(2, map[idxX-1][idxY]);    // 2
            surroundingTiles.put(3, map[idxX-1][idxY+1]);  // 3
            surroundingTiles.put(4, map[idxX][idxY-1]);    // 4
            surroundingTiles.put(5, map[idxX][idxY+1]);    // 5
            surroundingTiles.put(6, map[idxX+1][idxY-1]);  // 6
            surroundingTiles.put(7, map[idxX+1][idxY]);    // 7
            surroundingTiles.put(8, map[idxX+1][idxY+1]);  // 8
        }
        return surroundingTiles;
    }
}
