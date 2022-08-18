package lawnlayer;

import java.util.*;

/**
 * Represents an enemy agent.
 */
public class Enemy extends Agent {

    private final int[] TOP_KEYS = {1,2,3};
    private final int[] RIGHT_KEYS = {3,5,8};
    private final int[] DOWN_KEYS = {6,7,8};
    private final int[] LEFT_KEYS = {1,4,6};

    private boolean reflecting;
    private boolean reflectTopDown;
    private boolean reflectLeftRight;
    private boolean vulnerable;
    private TileObject prevTile;
    private HashMap<String,Boolean> prevMovements;

    /**
     * Creates a new enemy with specified (x,y) coordinates.
     * <p>
     * Initializes the enemy with a random direction it moves in. 
     * All enemies move diagonally. 
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Enemy(int x, int y) {
        super(x,y);
        // get random initial diagonal movement as index codes 
        int dirCodeOne, dirCodeTwo; 
        dirCodeOne = new Random().nextInt(movements.size());
        while (true) {
            dirCodeTwo = new Random().nextInt(movements.size());
            if (dirCodeOne != dirCodeTwo && Math.abs(dirCodeTwo-dirCodeOne)!=2)
                break;
        }
        // update movement to true
        movements.replace(movementIdx.get(dirCodeOne), true);
        movements.replace(movementIdx.get(dirCodeTwo), true);
        super.snapBoundary(this.getX(), this.getY());
    }

    /**
     * Sets the vulnerable state of this enemy.
     * @param value <code>true</code> if vulnerable, otherwise <code>false</code>
     */
    public void setVulnerable(boolean value) {
        this.vulnerable = value;
    }

    public boolean isVulnerable() {
        return this.vulnerable;
    }

    /**
     * Gets the vertical reflection state.
     * @return true if going to reflect vertical movement, otherwise false
     */
    public boolean isReflectTopDown() {
        return this.reflectTopDown;
    }

    /**
     * Gets the horizontal reflection state.
     * @return true if going to reflect horizontal movement, otherwise false
     */
    public boolean isReflectLeftRight() {
        return this.reflectLeftRight;
    }

    /**
     * Updates this enemy's movement and action. Called every frame.
     * <p>
     * Checks collision of this enemy with other game objects. Extends the movement 
     * behaviours of its parent class {@link lawnlayer.Agent}. Enemies also {@link #attack(App, Player, TileObject)} 
     * other game objects.
     * @param gameboard the current gameboard (i.e.App)
     */
    public void tick(App gameboard) {
        TileObject enemyTile = super.getTile(gameboard.allTileObjects());
        this.checkCollision(gameboard, enemyTile, 
                this.getSurroundingTiles(gameboard.getTileMap(), enemyTile));
        super.tick();
        super.snapBoundary(this.getX(), this.getY());
        attack(gameboard, gameboard.getPlayer(), enemyTile);
    }

    /**
     * Stops all movements of this enemy. The previous movements are remembered
     * so that they can be restored when the enemy unfreezes.
     */
    public void freeze() {
        prevMovements = new HashMap<String,Boolean>();
        prevMovements.putAll(movements);
        movements.replaceAll((k, v) -> false);
    }

    /**
     * Restores the movement of this enemy before it was frozen.
     */
    public void unfreeze() {
        movements.putAll(prevMovements);
    }

    /**
     * Respawns this enemy in another random tile in the gameplay area (i.e.soil).
     * @param tileMap the current 2d tilemap
     * @return the new tile where this enemy locates after respawn
     */
    public TileObject respawnTile(TileObject[][] tileMap) {
        // random spawn tile in soil area
        TileObject tile = null;
        while (true) {
            int rndTileRow = new Random().nextInt(tileMap.length-3) + 1; //1~30
            int rndTileCol = new Random().nextInt(tileMap[0].length-3) + 1; //1~62
            tile = tileMap[rndTileRow][rndTileCol];
            if (tile.isSoil())
                break; // tile empty, no need to generate another random tile
        }
        // get random pixel location within the chosen tile
        int xMin = tile.getX() - App.SPRITESIZE()/2;
        int xMax = tile.getX() + App.SPRITESIZE()/2;
        int yMin = tile.getY() - App.SPRITESIZE()/2;
        int yMax = tile.getY() + App.SPRITESIZE()/2;
        int rndX = new Random().nextInt((xMax+1) - xMin) + xMin;
        int rndY = new Random().nextInt((yMax+1) - yMin) + yMin;
 
        this.setX(rndX);
        this.setY(rndY);

        return tile;
    }

    /**
     * Checks if given integer array contains given integer.
     * @param n     the integer to be checked
     * @param arr   the integer array to be checked against
     * @return      <code>true</code> if element in array, otherwise <code>false</code>
     */
    public boolean contains(int n, int[] arr) {
        for (int elem : arr) {
            if (elem == n) return true;
        }
        return false;
    }

    /**
     * Check if this enemy collides with other game objects, and updates behaviours 
     * that needs to change upon collision.
     * <p>
     * The enemy reflects horizontally or vertically as it hits corresponding sides
     * of a wall. The sides of a wall is determined via the 8 tiles surrounding the 
     * enemy, categorized into 4 sides. The chance of a wall being present depends on
     * how many tiles on 1 side are hit (performs reflection for greater than 2 tiles:
     * 2=very likely a wall, 3=definetely a wall).
     * <p>
     * The enemy only reflects once until it reaches a different tile to avoid it 
     * getting stuck reflecting back and forth in the same tile. 
     * @param gameboard         the current gameboard (i.e.App)
     * @param enemyTile         this enemy's tile
     * @param surroundingTiles  the 8 tiles surrounding this enemy
     */
    public void checkCollision(App gameboard, TileObject enemyTile, 
                    HashMap<Integer,TileObject> surroundingTiles) {

        // use surrounding tiles to detect which side of wall the Agent is gonna hit
        /*   1 2 3
        *    4 o 5
        *    6 7 8
        **/
        // do not check surroundingTiles collision if still reflecting
        if (surroundingTiles.size() == 8 && !reflecting && enemyTile.isSoil()) {
            
            Iterator<Integer> itr = surroundingTiles.keySet().iterator();
            // shallow check
            while (itr.hasNext()) {
                Integer key = itr.next();
                if (surroundingTiles.get(key).isSoil())
                    itr.remove(); // left with tiles hit
            }

            // deep check if any of surroundingTiles is hit
            if (surroundingTiles.size() != 0) {
                int Top,Right,Down,Left;
                Top = Right = Down = Left = 0;
                for (Integer key: surroundingTiles.keySet()) {
                    if (this.contains(key, TOP_KEYS)) Top++;
                    if (this.contains(key, RIGHT_KEYS)) Right++;
                    if (this.contains(key, DOWN_KEYS)) Down++;
                    if (this.contains(key, LEFT_KEYS)) Left++;
                }
                
                // Determine side based on how many tiles hit on each side
                // int[] sides = {Top,Right,Down,Left};
                // System.out.printf("%d %d %d %d%n",sides[0],sides[1],sides[2],sides[3]);
                reflectTopDown = false;
                reflectLeftRight = false;
                if (Top >= 2 || Down >= 2) // 2=probably a wall; 3=definetely a wall
                    reflectTopDown = true;
                if (Right >= 2 || Left >= 2)
                    reflectLeftRight = true;
            }
        }

        // collide with tile objects (concrete,grass,path)
        if (!enemyTile.isSoil()) {

            // walloverride
            if (enemyTile.getRow()==0 || enemyTile.getRow()==App.GRID_ROW()-1) {
                reflectTopDown = true;
                // System.out.println("topDownWall override");
            }
            if (enemyTile.getCol()==0 || enemyTile.getCol()==App.GRID_COL()-1) {
                reflectLeftRight = true;
                // System.out.println("leftRightWall override");
            }
        
            // if not already reflecting
            if (!reflecting) {
                reflecting = true; // only reflect once
                // Top/Bottom wall
                if (reflectTopDown) {
                    for (Map.Entry<String,Boolean> entry : movements.entrySet()) {
                        if (entry.getKey().equals("moveUp") ||
                                entry.getKey().equals("moveDown"))
                            entry.setValue(!entry.getValue());   
                    }
                }
                // Left/Right wall
                if (reflectLeftRight) {
                    for (Map.Entry<String,Boolean> entry : movements.entrySet()) {
                        if (entry.getKey().equals("moveLeft") ||
                                entry.getKey().equals("moveRight"))
                            entry.setValue(!entry.getValue());   
                    }
                }
            }  
        } 

        // check if not in same tile anymore
        if (enemyTile != prevTile) 
                reflecting = false;
        prevTile = enemyTile;
    }

    /**
     * Defines the attack beahviours of this enemy.
     * All enemies in normal state causes {@link lawnlayer.Player#die(App)} upon collision 
     * when player in soil area. Enemies in vulnerable state respawns to another soil tile 
     * upon collision with player. See {@link #respawnTile(TileObject[][])}.
     * <p>
     * All enemies in normal state triggers red path to start propogating from the path 
     * tile that was hit. See {@link lawnlayer.Player#initialPropogate(TileObject)}. 
     * @param gameboard the current gameboard (i.e.App)
     * @param player    the player in this game instance
     * @param enemyTile this enemy's tile
     */
    public void attack(App gameboard, Player player, TileObject enemyTile) {
        // attack player directly when player in soil
        if (enemyTile == player.getPlayerTile() && player.isInSoil()) {
            if (vulnerable)
                this.respawnTile(gameboard.getTileMap());
            else
                player.die(gameboard);
        }
        // attack path if not vulnerable
        if (enemyTile.isPath() && !player.isPropogating() && !this.vulnerable) {
            player.initialPropogate(enemyTile);
        }
    }

}