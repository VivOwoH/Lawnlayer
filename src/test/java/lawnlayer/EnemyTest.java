package lawnlayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;

import processing.core.PApplet;
import java.util.*;

public class EnemyTest {
    
    App gameboard;
    Enemy enemyNormal;
    Enemy enemyBeetle;
    TileObject[][] map;
    TileObject[] allTileObjects;
    TileObject enemyTile;
    HashMap<Integer,TileObject>  surroundingTiles;
    Player player;

    @BeforeEach
    public void setUp() {
        enemyNormal = new Enemy(1300,800);
        enemyBeetle = new Beetle(-10,50);
    }

    @Test
    public void testConstructor() {
        // only two movements should be true
        int trueMovement = 0;
        for (boolean movement : enemyNormal.movements.values()) {
            if (movement) trueMovement++; 
        }
        assertTrue(trueMovement==2);
        // snap to boundary?
        assertTrue(enemyNormal.getX()==1270);
        assertTrue(enemyNormal.getY()==710);
        assertTrue(enemyBeetle.getX()==10);
        assertTrue(enemyBeetle.getY()==90);
    }

    @Test
    public void testFreeze() {
        Map<String,Boolean> prevMovements = new HashMap<String,Boolean>();
        prevMovements.putAll(enemyNormal.movements);
        enemyNormal.freeze();
        enemyNormal.unfreeze();
        for (Map.Entry<String,Boolean> entry : prevMovements.entrySet()) {
            assertEquals(entry.getValue(), enemyNormal.movements.get(entry.getKey()));
        }
    }

    @Test
    public void testRespawn() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        
        map = gameboard.parseTiles("level1.txt");
        allTileObjects = gameboard.allTileObjects();

        TileObject originalTile = enemyNormal.getTile(allTileObjects);
        TileObject newTile = enemyNormal.respawnTile(map);
        // respawn tile is a different tile
        assertTrue(originalTile != newTile);
        // respawn tile is soil
        assertTrue(newTile.isSoil());
    }

    @Test
    public void testCollision() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        
        map = gameboard.parseTiles("level1.txt");
        allTileObjects = gameboard.allTileObjects();

        // row 17, col 15 near a block of concrete 
        // s s c
        // s o c
        // s s c
        enemyNormal.movements.put("moveUp", true);
        enemyNormal.movements.put("moveRight", true);
        enemyNormal.movements.put("moveDown", false);
        enemyNormal.movements.put("moveLeft", false);
        enemyTile = map[17][15];
        surroundingTiles = enemyNormal.getSurroundingTiles(map, enemyTile);
        assertTrue(surroundingTiles.size()==8);
        assertTrue(surroundingTiles.get(1).isSoil() && 
                    surroundingTiles.get(4).isSoil() &&
                    surroundingTiles.get(6).isSoil() &&
                    surroundingTiles.get(7).isSoil() &&
                    surroundingTiles.get(2).isSoil());
        assertTrue(surroundingTiles.get(3).isConcrete() && 
                    surroundingTiles.get(5).isConcrete() &&
                    surroundingTiles.get(8).isConcrete());
        enemyNormal.checkCollision(gameboard, enemyTile, surroundingTiles);

        assertTrue(!enemyNormal.isReflectTopDown());
        assertTrue(enemyNormal.isReflectLeftRight());
        assertTrue(enemyNormal.movements.get("moveUp"));
        assertTrue(enemyNormal.movements.get("moveRight"));

        // collision checked, now set tile to collide so it would reflect
        // also test wall override
        enemyTile = map[0][63];
        enemyNormal.checkCollision(gameboard, enemyTile, surroundingTiles);
        assertTrue(enemyNormal.isReflectTopDown());
        assertTrue(enemyNormal.isReflectLeftRight());
        assertFalse(enemyNormal.movements.get("moveUp"));
        assertFalse(enemyNormal.movements.get("moveRight"));
        assertTrue(enemyNormal.movements.get("moveDown"));
        assertTrue(enemyNormal.movements.get("moveLeft"));
    }

    @Test
    public void testAttack() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        map = gameboard.parseTiles("level1.txt");
        allTileObjects = gameboard.allTileObjects();

        player = new Player(20, 100); // row 1 col 1
        player.setPlayerTile(gameboard.allTileObjects());
        player.checkPlayerInSoil();
        gameboard.setLives(3);

        // test attack player directly (not vulnerable)
        enemyTile = map[1][1];
        assertTrue(enemyTile==player.getPlayerTile()); // condition 1
        assertTrue(player.isInSoil()); // condition 2
        enemyNormal.attack(gameboard, player, enemyTile);
        assertTrue(gameboard.getLives()==2);
        // player changed
        assertTrue(gameboard.getPlayer().getTile(allTileObjects)==map[0][0]); // top left

        // test attack player directly (vulnerable)
        player = gameboard.getPlayer();
        player.setX(20);
        player.setY(100);
        enemyNormal.setVulnerable(true);
        enemyTile = map[1][1];
        enemyNormal.attack(gameboard, player, enemyTile);
        assertTrue(enemyNormal.getTile(allTileObjects)!=enemyTile);

        // test attack path
        enemyNormal.setVulnerable(false);
        player.setX(80);
        player.setY(100); // row 1 col 4 (away from enemy tile)
        enemyTile.setPath();
        enemyNormal.attack(gameboard, player, enemyTile);
        assertTrue(player.isPropogating());

        // test beetle special attack
        enemyBeetle.setX(40);
        enemyBeetle.setY(120); // row 2 col 2
        enemyTile = map[2][2];
        map[2][2].setGrass();
        enemyBeetle.attack(gameboard, player, enemyTile);
        assertTrue(map[2][2].isSoil());
    }

}
