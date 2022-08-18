package lawnlayer;

import java.util.*;

/**
 * Represents a player object that the user controls.
 */
public class Player extends Agent {

    private boolean KeyUp;
    private boolean KeyRight;
    private boolean KeyDown;
    private boolean KeyLeft;
    private boolean playerInSoil;
    private boolean pathCorner;

    private int velocity;

    private boolean propogating;
    private int propogateTimer;
    private int propogateSpeed = 3;

    private TileObject hitTileStart;
    private TileObject hitTileEnd;

    private TileObject backTile;
    private TileObject[] tailCorners;
    private TileObject frontTile;
    private TileObject playerTile;
    private List<TileObject> pathTiles;
    private List<TileObject> area1;
    private List<TileObject> area2;

    /**
     * Creates a new player object with specified (x,y) coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Player(int x, int y) {
        super(x,y);

        this.velocity = 1;
        this.propogateTimer = 0;

        this.pathTiles = new ArrayList<TileObject>();
        this.area1 = new ArrayList<TileObject>();
        this.area2 = new ArrayList<TileObject>();
        this.tailCorners = new TileObject[2];
        
        resetKey();
    }

    // -------------- Getter/Setter -----------------
    /**
     * Gets if a red path is propogating within the path tiles the player is laying.
     * @return <code>true</code> if the red path is still propogating, 
     *          otherwise <code>false</code>
     */
    public boolean isPropogating() {
        return this.propogating;
    }

    /**
     * Sets and stores the reference to the tile where this player current locates in.
     * @see lawnlayer.Agent#getTile(TileObject[])
     * @param allTileObjects the array of all tile objects
     */
    public void setPlayerTile(TileObject[] allTileObjects) {
        this.playerTile = super.getTile(allTileObjects);
    }

    /**
     * Gets the tile where the player locates in currently.
     * @return the player's tile
     */
    public TileObject getPlayerTile() {
        return this.playerTile;
    }

    /**
     * Gets the tile infront of the player on the side of player's current direction.
     * @return the front {@link TileObject} of the player 
     */
    public TileObject getFrontTile() {
        return this.frontTile;
    }

    /**
     * Gets the tile behind the player on the side oppositer player's current direction.
     * @return the back {@link TileObject} of the player 
     */
    public TileObject getBackTile() {
        return this.backTile;
    }

    /**
     * Gets the two tiles neighboring the back tile of the player.
     * @return the two tail corner {@link TileObject}
     */
    public TileObject[] getTailCorners() {
        return this.tailCorners;
    }

    /**
     * Gets the list of path tiles player layed down.
     * @return the list of path tiles
     */
    public List<TileObject> getPathTiles() {
        return this.pathTiles;
    }

    /**
     * Gets the next propogating tile (away from player)
     * @return the next propogating {@link TileObject}
     */
    public TileObject getHitTileStart() {
        return this.hitTileStart;
    }

    /**
     * Gets the next propogating tile (towards player)
     * @return the next propogating {@link TileObject}
     */
    public TileObject getHitTileEnd() {
        return this.hitTileEnd;
    }

    /**
     * Gets if the player is in the soil area. The soil area is also refered as 
     * game area or play area for this documentation. It is the zone where the 
     * player can lay path, fill grass, and get attacked. 
     * @return <code>true</code> if player in soil, otherwise <code>false</code>
     */
    public boolean isInSoil() {
        return this.playerInSoil;
    }

    /**
     * Sets the player to be at a path corner or not. This is for test cases only. 
     * No other classes should be able to manually override player state. 
     * @param value <code>true</code> if at path corner, otherwise <code>false</code>
     */
    public void setPathCorner(boolean value) {
        this.pathCorner = true;
    }

    /**
     * Gets if player is at a path corner
     * @return <code>true</code> if player at path corner, otherwise <code>false</code>
     */
    public boolean atPathCorner() {
        return this.pathCorner;
    }

    /**
     * Sets up key movement to true. This is for test cases only. No other classes 
     * should be able to manually override key movements. 
     * @param value <code>true</code> if move up, otherwise <code>false</code>
     */
    public void setKeyUp(boolean value) {
        this.KeyUp = value;
    }

    public boolean getKeyUp() {
        return this.KeyUp;
    }

    /**
     * Sets right key movement to true. This is for test cases only. No other classes 
     * should be able to manually override key movements. 
     * @param value <code>true</code> if move right, otherwise <code>false</code>
     */
    public void setKeyRight(boolean value) {
        this.KeyRight = value;
    }

    public boolean getKeyRight() {
        return this.KeyRight;
    }

    /**
     * Sets down key movement to true. This is for test cases only. No other classes 
     * should be able to manually override key movements. 
     * @param value <code>true</code> if move down, otherwise <code>false</code>
     */
    public void setKeyDown(boolean value) {
        this.KeyDown = value;
    }

    public boolean getKeyDown() {
        return this.KeyDown;
    }

    /**
     * Sets left key movement to true. This is for test cases only. No other classes 
     * should be able to manually override key movements. 
     * @param value <code>true</code> if move left, otherwise <code>false</code>
     */
    public void setKeyLeft(boolean value) {
        this.KeyLeft = value;
    }

    public boolean getKeyLeft() {
        return this.KeyLeft;
    }

    // ------------------ Methods ----------------------
    /**
     * Updates this player's movement and action. Called every frame.
     * <p>
     * The player starts laying down a path behind itself when it enters the soil area.
     * It checks enclosed areas and fills grass when it enters safe zone (grass or concrete).
     * Certain surrounding tiles are used to track the player's path.
     * Updates the game's current score when new grass is filled. See {@link lawnlayer.App#updateScore()}.
     * <p>
     * The player can {@link #die(App)} in the soil area if it hits its own path. 
     * Or if a propogating red path catches up and hits the player. See 
     * {@link #propogate(TileObject, TileObject)} and {@link #initialPropogate(TileObject)}.
     *  
     * @see lawnlayer.Agent#getSurroundingTiles(TileObject[][], TileObject)
     * @see #setFrontBackTile(HashMap)
     * @param gameboard the current gameboard
     */
    public void tick(App gameboard) {
        
        // enter from grass to concrete
        if (this.playerTile != null && this.playerTile.isGrass() && 
            super.getTile(gameboard.allTileObjects()).isConcrete()) {
            this.playerTile = super.getTile(gameboard.allTileObjects());
            resetKey();
            snapToGrid();            
        }

        setPlayerTile(gameboard.allTileObjects());

        // play area rules
        if (this.playerInSoil) {
            setFrontBackTile(super.getSurroundingTiles(gameboard.getTileMap(), this.playerTile));
            // stop when entering concrete or grass from soil
            if (super.getTile(gameboard.allTileObjects()).isConcrete() || 
                super.getTile(gameboard.allTileObjects()).isGrass()) {
                    updatePath(); // update path before fill grass
                    fillGrass(gameboard.getTileMap(), 
                            gameboard.allTileObjects(), 
                            gameboard.getEnemies());
                    this.propogating = false; // reset if was propogating when enter safe zone
                    gameboard.updateScore(); // update goal only when new grass is filled
                    resetKey();
                    snapToGrid();
            } else if (this.frontTile.isPath()) { // player hits own path
                die(gameboard);
                return;
            } else if (this.pathTiles.size()!=0 && 
                    this.pathTiles.get(this.pathTiles.size()-1).isPathHit()) { // if red path hit player
                die(gameboard);
                return;
            } else {
                updatePath();
            }
        }

        this.move(this.getX(), this.getY());

        checkPlayerInSoil(); // check if player in soil after it moves

        // Propogation
        if (this.propogating) {
            propogateTimer = (propogateTimer + 1) % propogateSpeed;
            if (propogateTimer == 1) 
                propogate(this.hitTileStart, this.hitTileEnd);
        }
    }

    /**
     * Updates the player's location based on its movements. The movements are 
     * determined via the user's key controls. Snaps the player to boundaries if
     * it is at any of the wall edges, with the function defined in its parent 
     * class {@link Agent}.
     * @param x the current x-coordinate of this player
     * @param y the current y-coordinate of this player
     */
    public void move(int x, int y) {
        if (KeyUp && (!KeyRight && !KeyLeft))
            y -= velocity * super.PPF; 
        if (KeyRight)
            x += velocity * super.PPF;
        if (KeyDown && (!KeyRight && !KeyLeft))
            y += velocity * super.PPF;
        if (KeyLeft)
            x -= velocity * super.PPF;

        super.snapBoundary(x, y);
    }

    /**
     * Checks if this player is in soil area.
     * Does not directly check if the player's tile is a soil tile. Instead, checks 
     * if the player's tile is not a safe tile (grass or concrete). Because the 
     * player's tile can be a path tile.
     */
    public void checkPlayerInSoil() {
        // Do not directly check isSoil() because could be path
        this.playerInSoil = (!this.playerTile.isConcrete() &&
                            !this.playerTile.isGrass()) ? true:false;
    }

    /**
     * Snaps this player to the center of the tile. Ensures the player is always 
     * visually aligned with the grid map.
     */
    public void snapToGrid() {
        // instant force align with center of tile
        if (this.getY() != this.playerTile.getY())
            this.setY(this.playerTile.getY());

        if (this.getX() != this.playerTile.getX())
            this.setX(this.playerTile.getX());
    }

    /**
     * Sets the front and back tiles of this player, based on the 8 tiles surrounding
     * this player. See {@link lawnlayer.Agent#getSurroundingTiles(TileObject[][], TileObject)}. 
     * @param surroundingTiles the 8 tiles surrounding this player
     */
    public void setFrontBackTile(HashMap<Integer,TileObject> surroundingTiles) {
        /* surrounding 8 tiles
        *    1 2 3
        *    4 o 5
        *    6 7 8
        **/
        // use direction to determine which tile is front and back
        if (this.KeyUp) {
            frontTile = surroundingTiles.get(2);
            backTile = surroundingTiles.get(7);
            tailCorners[0] = surroundingTiles.get(6);
            tailCorners[1] = surroundingTiles.get(8);
        } else if (this.KeyRight) {
            frontTile = surroundingTiles.get(5);
            backTile = surroundingTiles.get(4);
            tailCorners[0] = surroundingTiles.get(1);
            tailCorners[1] = surroundingTiles.get(6);
        } else if (this.KeyDown) {
            frontTile = surroundingTiles.get(7);
            backTile = surroundingTiles.get(2);
            tailCorners[0] = surroundingTiles.get(1);
            tailCorners[1] = surroundingTiles.get(3);
        } else if (this.KeyLeft) {
            frontTile = surroundingTiles.get(4);
            backTile = surroundingTiles.get(5);
            tailCorners[0] = surroundingTiles.get(3);
            tailCorners[1] = surroundingTiles.get(8);
        }
    }

    /**
     * Updates the path tile. 
     * Sets the back tile of this player to be a path tile. If the player is at a 
     * turning corner, sets the player's tile to be a path tile.
     * @see lawnlayer.TileObject#setPath()
     */
    public void updatePath() {
        // if at path corner
        if (pathCorner) backTile = this.playerTile;
        if (backTile.isSoil() && !this.playerTile.isPath()) { 
            backTile.setPath();
            if (!pathTiles.contains(backTile)) this.pathTiles.add(backTile);
        }
        pathCorner = false; // reset value
    }

    /**
     * Removes this player and spawns in a new player. Deducts a life. 
     * @see lawnlayer.App#newPlayer()
     * @see lawnlayer.App#modifyLife(int)
     * @param gameboard the current gameboard
     * @return the new player
     */
    public Player die(App gameboard) {
        for (TileObject tile : pathTiles) tile.setSoil();
        gameboard.newPlayer();
        gameboard.modifyLife(-1); // deduct a life
        return this;
    }

    /**
     * Starts the propogation of red path. Marks the neighboring tiles as
     * the next 2 tiles to set as red path. See {@link lawnlayer.TileObject#setPathHit()}.
     * @param hitTile the tile that was hit by {@link Enemy}
     */
    public void initialPropogate(TileObject hitTile) {
        this.propogating = true;
        hitTile.setPathHit();
        if (pathTiles.indexOf(hitTile) > 0) 
            this.hitTileStart = pathTiles.get(pathTiles.indexOf(hitTile)-1);
        if (pathTiles.indexOf(hitTile) < this.pathTiles.size()-1)
            this.hitTileEnd = pathTiles.get(pathTiles.indexOf(hitTile)+1);
    }

    /**
     * Sets the 2 marked tiles as red path. Marks another 2 neighboring tiles as
     * the next 2 red path tiles. See {@link lawnlayer.TileObject#setPathHit()}.
     * @param hitTileStart the front tile to be set as red path (towards player)
     * @param hitTileEnd   the tail tile to be set as red path (away from player)
     */
    public void propogate(TileObject hitTileStart, TileObject hitTileEnd) {

        if (hitTileStart != null) {
            hitTileStart.setPathHit();
            // update next hitTile away from player (null if first)
            this.hitTileStart = (pathTiles.indexOf(hitTileStart) > 0) ? 
                pathTiles.get(pathTiles.indexOf(hitTileStart)-1) : null;
        }

        if (hitTileEnd != null) {
            hitTileEnd.setPathHit();
            // update next hitTile towards player (null if last)
            this.hitTileEnd = (pathTiles.indexOf(hitTileEnd) < this.pathTiles.size()-1) ?
                pathTiles.get(pathTiles.indexOf(hitTileEnd)+1) : null;
        }
    }

    /**
     * Checks areas enclosed by the player's path laid and fills areas with grass.
     * <p>
     * Uses the <b>flood fill algorithm</b> to find enclosed areas. Starts the searches 
     * from the 2 tail corners of the player. 
     * <p> If both corners are soil, search twice 
     * starting from both corner tiles.
     * <p> If either corner is soil, search once starting 
     * from the soil tile.
     * <p> If both corners are not soil, simply fill the path with grass
     * and <code>return</code>. (<i>Not optimal but safer 
     *               than starting flood fill from any soil tile. 
     *               Wrong areas can be marked and filled.</i>)
     * <p>
     * The case of only 1 area with all soil tiles included is possible, when the path
     * does not properly enclose two areas. Ideally, there are 2 enclosed areas.
     * <p>
     * 1. Both areas with enemies: fill the path only<br>
     * 2. Or fill either side without enemies <br>
     * In both conditions, the path tiles are filled with grass too. After filling grass,
     * clear the list of path tiles and both enclosed areas.
     * 
     * @see #getEncloseArea(TileObject[][], int, int, List)
     * @see lawnlayer.TileObject#setGrass()
     * @see #setFrontBackTile(HashMap)
     * @param map               the current 2d tilemap
     * @param allTileObjects    all tile objects in the tilemap
     * @param enemies           the list of enemies
     */
    public void fillGrass(TileObject[][] map, TileObject[] allTileObjects, List<Enemy> enemies) {
        // flood fill algorithm
        // start the search from 2 back tiles around player
        // c o c
        // ? p ? (c=concrete; p=path; check ?)
        // should be 2 maximum, 1 probably, 0 most rare but could happen
        
        // long timer = System.currentTimeMillis();

        // Get two enclosed areas
        // if both corners are empty, check twice
        if (tailCorners[0].isSoil() && tailCorners[1].isSoil()) {
            getEncloseArea(map, tailCorners[0].getRow(), 
                        tailCorners[0].getCol(), this.area1);
            getEncloseArea(map, tailCorners[1].getRow(), 
                        tailCorners[1].getCol(), this.area2);
        }
        // corner 1 is empty
        else if (tailCorners[0].isSoil() && !tailCorners[1].isSoil()) {
            getEncloseArea(map, tailCorners[0].getRow(), 
                        tailCorners[0].getCol(), this.area1);
        }
        // corner 2 is empty
        else if (!tailCorners[0].isSoil() && tailCorners[1].isSoil()) {
            getEncloseArea(map, tailCorners[1].getRow(), 
                        tailCorners[1].getCol(), this.area1);
        }
        // if somehow no soil around, just draw the path and return
        // *Edge case: Not optimal but safer, use flood fill for any soil first found 
        //             can mark the area that just got removed by beetle to be flooed,
        //             and causing beetles to get caught in the middle
        else {
            System.out.println("No surrounding soil");
            for (TileObject tile : this.pathTiles) tile.setGrass();
            pathTiles.clear();
            area1.clear();
            area2.clear();
            return;
            // for (TileObject tile : allTileObjects) {
            //     if (tile.isSoil()) {
            //         getEncloseArea(map, tile.getRow(), tile.getCol(), this.area1);
            //         break;
            //     }
            // }
        }
        // System.out.println(System.currentTimeMillis() - timer);

        if (area2.size() == 0) {
            for (TileObject tile : allTileObjects) {
                if (!tile.isConcrete() && !tile.isPath() &&
                    !area1.contains(tile))
                    area2.add(tile); // inverted area
            }
        }

        // System.out.printf("Area1: %d%n", area1.size());
        // System.out.printf("Area2: %d%n", area2.size());

        // at this point, if area1=area2 or area2 = 0, 
        // meaning not properly enclosed
        // ignore all following area 2 (inverted area) check
        boolean checkInvertedArea = true;
        if (area1.size()==area2.size() || area2.size()==0)
            checkInvertedArea = false;

        boolean enemyInArea = false;
        boolean enemyInInvertedArea = false;
        for (Enemy enemy : enemies) {
            if (area1.contains(enemy.getTile(allTileObjects))) {
                enemyInArea = true;
            }
            if (checkInvertedArea && 
                area2.contains(enemy.getTile(allTileObjects))) {
                enemyInInvertedArea = true;
            }
        }
        
        // 1. Both areas with enemies -> fill the path only
        // 2. Fill sides without enemies 
        if (!enemyInArea) {
            for (TileObject tile : area1) tile.setGrass();
            // System.out.println("area filled");
        }
        if (checkInvertedArea && !enemyInInvertedArea) {
            for (TileObject tile : area2) tile.setGrass();
            // System.out.println("inverted area filled");
        }
        // All conditions fill the path
        for (TileObject tile : this.pathTiles) tile.setGrass();
        
        // After filling grass, clear path + both areas
        pathTiles.clear();
        area1.clear();
        area2.clear();
    }

    /**
     * Recursively searches the 4 neighboring tiles of the current tile.
     * The base case is when it hits the walls of the game area, 
     * or any tile that is not soil. 
     * @param map       the current 2d tilemap
     * @param curRow    the current row of recursion
     * @param curCol    the current column of recursion
     * @param list      the area list to which the marked tiles are added
     */
    public void getEncloseArea(TileObject[][] map, int curRow, int curCol, 
                                List<TileObject> list) {
        // System.out.printf("%d %d%n",curRow, curCol);
        if (curRow < 0 || curCol < 0 || 
            curRow >= map.length || curCol >= map[0].length || 
            list.contains(map[curRow][curCol]) || 
            !map[curRow][curCol].isSoil()) return;

        list.add(map[curRow][curCol]);
        getEncloseArea(map, curRow-1, curCol, list);
        getEncloseArea(map, curRow+1, curCol, list);
        getEncloseArea(map, curRow, curCol-1, list);
        getEncloseArea(map, curRow, curCol+1, list);
    }

    // ---------------- Key Control --------------------
    /**
     * Resets all movement keys.
     */
    public void resetKey() {
        this.KeyUp = false;
        this.KeyRight = false;
        this.KeyDown = false;
        this.KeyLeft = false;
    }

    /**
     * This player moves up. Called if UP key is pressed. Player is not allowed 
     * diagonal movement.
     * <p> When the player is in grass or soil area, it is not allowed to backtrack.
     * The player {@link #snapToGrid()} when it changes direction in grass or soil, 
     * and a path corner is marked. This affects {@link #setFrontBackTile(HashMap)}. 
     * @see lawnlayer.App#keyPressed()
     */
    public void pressUp() {
        if ((this.playerInSoil || this.playerTile.isGrass()) && this.KeyDown) {
            return; // cancel out movement in soil or grass
        } else if (this.playerInSoil && (this.KeyLeft || this.KeyRight)) {
            // not in grass because set pathcorner in grass cause bugs
            snapToGrid();
            pathCorner = true;
        } 
        resetKey();
        this.KeyUp = true;
    } 
    
    /**
     * This player moves right. Called if RIGHT key is pressed. Player is not allowed 
     * diagonal movement.
     * <p> When the player is in grass or soil area, it is not allowed to backtrack.
     * The player {@link #snapToGrid()} when it changes direction in grass or soil, 
     * and a path corner is marked. This affects {@link #setFrontBackTile(HashMap)}.
     * @see lawnlayer.App#keyPressed() 
     */
    public void pressRight() {
        if ((this.playerInSoil || this.playerTile.isGrass()) && this.KeyLeft) {
            return; // cancel out movement in soil or grass
        } else if (this.playerInSoil && (this.KeyUp || this.KeyDown)) {
            snapToGrid();
            pathCorner = true;
        } 
        resetKey();
        this.KeyRight = true;
    }
   
    /**
     * This player moves down. Called if DOWN key is pressed. Player is not allowed 
     * diagonal movement.
     * <p> When the player is in grass or soil area, it is not allowed to backtrack.
     * The player {@link #snapToGrid()} when it changes direction in grass or soil, 
     * and a path corner is marked. This affects {@link #setFrontBackTile(HashMap)}. 
     * @see lawnlayer.App#keyPressed()
     */
    public void pressDown() {
        if ((this.playerInSoil || this.playerTile.isGrass()) && this.KeyUp) {
            return; // cancel out movement in soil or grass
        } else if (this.playerInSoil && (this.KeyLeft || this.KeyRight)) {
            snapToGrid();
            pathCorner = true;
        } 
        resetKey();
        this.KeyDown = true;
    }  

    /**
     * This player moves left. Called if LEFT key is pressed. Player is not allowed 
     * diagonal movement.
     * <p> When the player is in grass or soil area, it is not allowed to backtrack.
     * The player {@link #snapToGrid()} when it changes direction in grass or soil, 
     * and a path corner is marked. This affects {@link #setFrontBackTile(HashMap)}. 
     * @see lawnlayer.App#keyPressed()
     */
    public void pressLeft() {
        if ((this.playerInSoil || this.playerTile.isGrass())  && this.KeyRight) {
            return; // cancel out movement in soil or grass
        } else if (this.playerInSoil && (this.KeyUp || this.KeyDown)) {
            snapToGrid();
            pathCorner = true;
        } 
        resetKey();
        this.KeyLeft = true;
    }

    /**
     * Stops moving up and {@link #snapToGrid()}. If this player is in grass or soil 
     * area, the player continues movement.
     */
    public void releaseUp() {
        // do nothing if in soil and grass
        if (this.playerInSoil || this.playerTile.isGrass()) 
            return; 
        snapToGrid();
        this.KeyUp = false;
    }

    /**
     * Stops moving right and {@link #snapToGrid()}. If this player is in grass or soil 
     * area, the player continues movement.
     */
    public void releaseRight() {
        // do nothing if in soil and grass
        if (this.playerInSoil || this.playerTile.isGrass()) 
            return; 
        snapToGrid();
        this.KeyRight = false;
    }

    /**
     * Stops moving down and {@link #snapToGrid()}. If this player is in grass or soil 
     * area, the player continues movement.
     */
    public void releaseDown() {
        // do nothing if in soil and grass
        if (this.playerInSoil || this.playerTile.isGrass()) 
            return; 
        snapToGrid();  
        this.KeyDown = false;
    }

    /**
     * Stops moving left and {@link #snapToGrid()}. If this player is in grass or soil 
     * area, the player continues movement.
     */
    public void releaseLeft() {
        // do nothing if in soil and grass
        if (this.playerInSoil || this.playerTile.isGrass()) 
            return; 
        snapToGrid();
        this.KeyLeft = false;
    }
}
