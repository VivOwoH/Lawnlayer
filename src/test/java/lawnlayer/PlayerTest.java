package lawnlayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;

import processing.core.PApplet;
import java.util.*;

public class PlayerTest {
    
    App gameboard;
    Player player;
    TileObject playerTile;
    TileObject[][] map;
    HashMap<Integer,TileObject> surroundingTiles;
    List<TileObject> pathTiles;

    @BeforeEach
    public void setUp() {
        player = new Player(20, 100); // row 1 col 1
    }

    @Test
    public void testConstructor() {
        assertTrue(player.getX()==30);
        assertTrue(player.getY()==110);
    }

    @Test
    public void testMove() {
        // check diagonal movement restricted
        
        player.setKeyUp(true);
        player.setKeyRight(true);
        player.move(player.getX(), player.getY());
        assertTrue(player.getX()==32 &&
                player.getY()==110);
        
        player.setKeyDown(true);
        player.setKeyLeft(true);
        player.move(player.getX(), player.getY());
        assertTrue(player.getX()==32 &&
                player.getY()==110);

        player.setKeyRight(false);
        player.setKeyLeft(false);
        player.move(player.getX(), player.getY());
        assertTrue(player.getX()==32 &&
                player.getY()==110);
    }

    @Test
    public void testPlayerTile() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        map = gameboard.parseTiles("level1.txt");
        
        player.setPlayerTile(gameboard.allTileObjects());
        assertTrue(player.getPlayerTile()==map[1][1]);

        // player in soil?
        player.checkPlayerInSoil();
        assertTrue(player.getPlayerTile().isSoil());

        // snap to grid?
        // x=30, y=110 (tile center)
        assertTrue(player.getX()==30 && player.getY()==110);
        player.setX(40);
        player.setY(120);
        player.snapToGrid();
        assertTrue(player.getX()==30 && 
                   player.getY()==110);
    }

    @Test
    public void testFrontBackAndPathTiles() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        map = gameboard.parseTiles("level1.txt");
        playerTile = player.getTile(gameboard.allTileObjects());

        surroundingTiles = player.getSurroundingTiles(map, playerTile);
        player.setKeyUp(true);
        player.setFrontBackTile(surroundingTiles);
        assertEquals(map[0][1], player.getFrontTile());
        assertEquals(map[2][1], player.getBackTile());
        assertEquals(map[2][0], player.getTailCorners()[0]);
        assertEquals(map[2][2], player.getTailCorners()[1]);

        player.setKeyUp(false);
        player.setKeyRight(true);
        player.setFrontBackTile(surroundingTiles);
        assertEquals(map[1][2], player.getFrontTile());
        assertEquals(map[1][0], player.getBackTile());
        assertEquals(map[0][0], player.getTailCorners()[0]);
        assertEquals(map[2][0], player.getTailCorners()[1]);

        player.setKeyRight(false);
        player.setKeyDown(true);
        player.setFrontBackTile(surroundingTiles);
        assertEquals(map[2][1], player.getFrontTile());
        assertEquals(map[0][1], player.getBackTile());
        assertEquals(map[0][0], player.getTailCorners()[0]);
        assertEquals(map[0][2], player.getTailCorners()[1]);

        player.setKeyDown(false);
        player.setKeyLeft(true);
        player.setFrontBackTile(surroundingTiles);
        assertEquals(map[1][0], player.getFrontTile());
        assertEquals(map[1][2], player.getBackTile());
        assertEquals(map[0][2], player.getTailCorners()[0]);
        assertEquals(map[2][2], player.getTailCorners()[1]);
        
        // test update path
        player.setPlayerTile(gameboard.allTileObjects());
        player.updatePath();
        assertTrue(map[1][2].isPath());
        assertTrue(player.getPathTiles().size()==1);

        // path corner
        player.setPathCorner(true);
        player.updatePath();
        assertTrue(map[1][1].isPath());
        assertTrue(player.getPathTiles().size()==2);
        assertFalse(player.atPathCorner()); // reseted

        // test player die all path tiles should be cleaned
        player.die(gameboard);
        assertTrue(map[1][1].isSoil());
        assertTrue(map[1][2].isSoil());
    }

    @Test
    public void testPropogate() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        map = gameboard.parseTiles("level1.txt");
        
        // manually turn some tiles to path
        TileObject[] arr = {map[2][1], map[2][2], map[3][2], map[4][2], map[4][1]};
        for (TileObject tile : arr) {
            tile.setPath();
            player.getPathTiles().add(tile);
        }
        player.initialPropogate(arr[1]);
        // 3 times should set all path to red path
        for (int i = 0; i < 3; i++) {
            player.propogate(player.getHitTileStart(), player.getHitTileEnd());
        }
        for (TileObject tile : arr) {
            assertTrue(tile.isPathHit());
        }
    }

    @Test
    public void testFillGrass() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        map = gameboard.parseTiles("level1.txt");

        // spawn in two enemies
        List<Enemy> enemies = new ArrayList<Enemy>();
        Enemy enemy1 = new Enemy(300,100); // left
        Enemy enemy2 = new Enemy(900,300); // right
        enemies.add(enemy1);
        enemies.add(enemy2);
        pathTiles = new ArrayList<TileObject>();
        // manually set a center path
        for (int i = 1; i < 31; i++) {
            map[i][31].setPath();
            player.getPathTiles().add(map[i][31]);
        }
        pathTiles.addAll(player.getPathTiles()); // remember it because it 
                                                // would be cleared by fillGrass()
        map[1][30].setSoil();
        map[1][32].setSoil();
        player.getTailCorners()[0] = map[1][30];
        player.getTailCorners()[1] = map[1][32];
        
        // --------------------------------------
        // both corner empty situation, both area have enemies
        player.fillGrass(map, gameboard.allTileObjects(), enemies);
        int numOfGrass = 0;
        for (TileObject tile : gameboard.allTileObjects()) {
            if (tile.isGrass()) numOfGrass++;   
        }
        assertTrue(numOfGrass==30); // only the path

        // reset
        for (TileObject tile : gameboard.allTileObjects()) {
            if (!tile.isConcrete()) {
                tile.setSoil();
            }
        }
        for (TileObject pathTile : pathTiles) 
            pathTile.setPath();
        player.getPathTiles().addAll(pathTiles);


        // corner 1 empty situation, has enemies
        map[1][30].setSoil();
        map[1][32].setConcrete();
        // not properly enclosed (e.g.a concrete block in the middle of map)
        map[30][31].setSoil();
        player.getPathTiles().remove(map[30][31]);

        player.fillGrass(map, gameboard.allTileObjects(), enemies);
        numOfGrass = 0;
        for (TileObject tile : gameboard.allTileObjects()) {
            if (tile.isGrass()) numOfGrass++;
        }
        assertTrue(numOfGrass==29);

        // reset
        for (TileObject tile : gameboard.allTileObjects()) {
            if (!tile.isConcrete()) {
                tile.setSoil();
            }
        }
        for (TileObject pathTile : pathTiles) 
            pathTile.setPath();
        player.getPathTiles().addAll(pathTiles);

        // corner 2 situation
        map[1][30].setConcrete();
        map[1][32].setSoil();
        // both areas no enemies
        enemies.clear();
        player.fillGrass(map, gameboard.allTileObjects(), enemies);
        numOfGrass = 0;
        int numOfConcrete = 0;
        for (TileObject tile : gameboard.allTileObjects()) {
            if (tile.isGrass()) numOfGrass++;
            if (tile.isConcrete()) numOfConcrete++;
        }
        assertTrue(numOfGrass+numOfConcrete==2048); // everything is grass

        // reset
        for (TileObject tile : gameboard.allTileObjects()) {
            if (!tile.isConcrete()) {
                tile.setSoil();
            }
        }
        for (TileObject pathTile : pathTiles) 
            pathTile.setPath();
        player.getPathTiles().addAll(pathTiles);
        assertTrue(player.getPathTiles().size()==30);

        // both corners not empty situation
        map[1][30].setConcrete();
        map[1][32].setConcrete();
        player.fillGrass(map, gameboard.allTileObjects(), enemies);
        numOfGrass = 0;
        for (TileObject tile : gameboard.allTileObjects()) {
            if (tile.isGrass()) numOfGrass++;
        }
        assertTrue(numOfGrass==30);
    }

    @Test
    public void testKeys() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        player.setPlayerTile(gameboard.allTileObjects());

        // Test UP key
        // cancel out movement in soil
        player.setKeyDown(true); // moving down
        player.checkPlayerInSoil();
        player.pressUp();
        assertTrue(player.getKeyDown());

        // cancel out movement in grass
        player.getPlayerTile().setGrass();
        player.checkPlayerInSoil();
        player.pressUp();
        assertTrue(player.getKeyDown());

        // change direction to UP
        player.setKeyDown(false);
        player.setKeyLeft(true);
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.pressUp();
        assertTrue(player.atPathCorner());
        assertTrue(player.getKeyUp());

        player.resetKey();
        player.setPathCorner(false);

        player.setKeyUp(false);
        player.setKeyRight(true);
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.pressUp();
        assertTrue(player.atPathCorner());
        assertTrue(player.getKeyUp());

        player.resetKey();
        player.setPathCorner(false);

        // Test RIGHT key
        // cancel out movement in soil
        player.setKeyLeft(true); // moving left
        player.checkPlayerInSoil();
        player.pressRight();
        assertTrue(player.getKeyLeft());

        // cancel out movement in grass
        player.setPlayerTile(gameboard.allTileObjects());
        player.getPlayerTile().setGrass();
        player.checkPlayerInSoil();
        player.pressRight();
        assertTrue(player.getKeyLeft());

        // change direction to RIGHT
        player.setKeyLeft(false);
        player.setKeyUp(true);
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.pressRight();
        assertTrue(player.atPathCorner());
        assertTrue(player.getKeyRight());

        player.resetKey();
        player.setPathCorner(false);

        player.setKeyRight(false);
        player.setKeyDown(true);
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.pressRight();
        assertTrue(player.atPathCorner());
        assertTrue(player.getKeyRight());

        player.resetKey();
        player.setPathCorner(false);

        // Test DOWN key
        // cancel out movement in soil
        player.setKeyUp(true); // moving up
        player.checkPlayerInSoil();
        player.pressDown();
        assertTrue(player.getKeyUp());

        // cancel out movement in grass
        player.setPlayerTile(gameboard.allTileObjects());
        player.getPlayerTile().setGrass();
        player.checkPlayerInSoil();
        player.pressDown();
        assertTrue(player.getKeyUp());

        // change direction to DOWN
        player.setKeyUp(false);
        player.setKeyLeft(true);
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.pressDown();
        assertTrue(player.atPathCorner());
        assertTrue(player.getKeyDown());

        player.resetKey();
        player.setPathCorner(false);

        player.setKeyDown(false);
        player.setKeyRight(true);
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.pressDown();
        assertTrue(player.atPathCorner());
        assertTrue(player.getKeyDown());

        player.resetKey();
        player.setPathCorner(false);

        // Test LEFT key
        // cancel out movement in soil
        player.setKeyRight(true); // moving right
        player.checkPlayerInSoil();
        player.pressLeft();
        assertTrue(player.getKeyRight());

        // cancel out movement in grass
        player.setPlayerTile(gameboard.allTileObjects());
        player.getPlayerTile().setGrass();
        player.checkPlayerInSoil();
        player.pressLeft();
        assertTrue(player.getKeyRight());

        // change direction to LEFT
        player.setKeyRight(false);
        player.setKeyUp(true);
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.pressLeft();
        assertTrue(player.atPathCorner());
        assertTrue(player.getKeyLeft());

        player.resetKey();
        player.setPathCorner(false);

        player.setKeyLeft(false);
        player.setKeyDown(true);
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.pressLeft();
        assertTrue(player.atPathCorner());
        assertTrue(player.getKeyLeft());

        player.resetKey();
        player.setPathCorner(false);

        //-------------------------------
        // Test Release up key
        player.setKeyUp(true);
        // player in soil
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.releaseUp();
        assertTrue(player.getKeyUp());
        // player in grass
        player.getPlayerTile().setGrass();
        player.checkPlayerInSoil();
        player.releaseUp();
        assertTrue(player.getKeyUp());
        // not in soil or grass
        player.getPlayerTile().setConcrete();
        player.checkPlayerInSoil();
        player.releaseUp();
        assertFalse(player.getKeyUp());

        player.resetKey();

        // Test Release right key
        player.setKeyRight(true);
        // player in soil
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.releaseRight();
        assertTrue(player.getKeyRight());
        // player in grass
        player.getPlayerTile().setGrass();
        player.checkPlayerInSoil();
        player.releaseRight();
        assertTrue(player.getKeyRight());
        // not in soil or grass
        player.getPlayerTile().setConcrete();
        player.checkPlayerInSoil();
        player.releaseRight();
        assertFalse(player.getKeyRight());

        player.resetKey();

        // Test Release down key
        player.setKeyDown(true);
        // player in soil
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.releaseDown();
        assertTrue(player.getKeyDown());
        // player in grass
        player.getPlayerTile().setGrass();
        player.checkPlayerInSoil();
        player.releaseDown();
        assertTrue(player.getKeyDown());
        // not in soil or grass
        player.getPlayerTile().setConcrete();
        player.checkPlayerInSoil();
        player.releaseDown();
        assertFalse(player.getKeyDown());

        player.resetKey();

        // Test Release left key
        player.setKeyLeft(true);
        // player in soil
        player.getPlayerTile().setSoil();
        player.checkPlayerInSoil();
        player.releaseLeft();
        assertTrue(player.getKeyLeft());
        // player in grass
        player.getPlayerTile().setGrass();
        player.checkPlayerInSoil();
        player.releaseLeft();
        assertTrue(player.getKeyLeft());
        // not in soil or grass
        player.getPlayerTile().setConcrete();
        player.checkPlayerInSoil();
        player.releaseLeft();
        assertFalse(player.getKeyLeft());
    }
}
