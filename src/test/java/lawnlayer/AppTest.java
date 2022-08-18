package lawnlayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import processing.core.PApplet;
import java.util.*;

public class AppTest {
    
    App gameboard;
    Player player;

    @BeforeEach
    public void setUp() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins

        player = gameboard.getPlayer();
    }

    @Test
    public void test() {
        
        // check powerup update
        assertNull(gameboard.getPowerup());
        gameboard.setDelayInterval(0);
        // 3. delay timer finish, spawn powerup
        gameboard.updatePowerup();

        // powerup - invincible
        gameboard.getPowerup().setPowerupKey("Invincible");
        player.setX(20);
        player.setY(100); // row 1 col 1
        player.setPlayerTile(gameboard.allTileObjects());

        gameboard.getPowerup().setX(20);
        gameboard.getPowerup().setY(100);

        gameboard.updatePowerup();
        assertTrue(gameboard.isPowerupInEffect());
        assertTrue(gameboard.getPowerupTimer()==600);
        assertEquals(gameboard.getSprites().get("rainbowPlayer"), 
                        player.getSprite());
        for (Enemy enemy : gameboard.getEnemies()) {
            assertEquals(gameboard.getSprites().get("enemyClown"), enemy.getSprite());
            assertTrue(enemy.isVulnerable());
        }

        // check timer decrement
        gameboard.updatePowerup();
        assertTrue(gameboard.getPowerupTimer()==599);

        // check powerup faded, then parse in new powerup
        gameboard.setPowerupTimer(0);
        gameboard.setDelayInterval(0);
        gameboard.updatePowerup();
        assertFalse(gameboard.isPowerupInEffect());
        assertEquals(gameboard.getSprites().get("player"), 
                        player.getSprite());
        for (Enemy enemy : gameboard.getEnemies()) {
            assertFalse(enemy.isVulnerable());
        }


        // powerup - time stop
        gameboard.getPowerup().setPowerupKey("ZAWARUDO");
        player.setX(20);
        player.setY(100); // row 1 col 1
        player.setPlayerTile(gameboard.allTileObjects());

        gameboard.getPowerup().setX(20);
        gameboard.getPowerup().setY(100);

        // check first enemy, remember its movement
        HashMap<String, Boolean> prevMovement = new HashMap<String, Boolean>();
        prevMovement.putAll(gameboard.getEnemies().get(0).movements);
        
        gameboard.updatePowerup();
        assertTrue(gameboard.isPowerupInEffect());
        assertTrue(gameboard.getPowerupTimer()==600);
        assertEquals(gameboard.getSprites().get("player"), 
                        player.getSprite());
        for (Enemy enemy : gameboard.getEnemies()) {
            assertEquals(gameboard.getSprites().get("enemyFrozen"), enemy.getSprite());
            for (boolean movement : enemy.movements.values())
                assertFalse(movement);
        }

        // check timer decrement
        gameboard.updatePowerup();
        assertTrue(gameboard.getPowerupTimer()==599);

        // check powerup faded, then parse in new powerup
        gameboard.setPowerupTimer(0);
        gameboard.updatePowerup();
        assertFalse(gameboard.isPowerupInEffect());
        for (Map.Entry<String,Boolean> entry : gameboard.getEnemies()
                                .get(0).movements.entrySet()) {
            assertEquals(prevMovement.get(entry.getKey()), entry.getValue());
        }

        // Test keys
        gameboard.keyCode = 37; // Left
        gameboard.keyPressed();
        assertTrue(player.getKeyLeft());
        gameboard.keyReleased();
        assertFalse(player.getKeyLeft());

        gameboard.keyCode = 38; // up 
        gameboard.keyPressed();
        assertTrue(player.getKeyUp());
        gameboard.keyReleased();
        assertFalse(player.getKeyUp());

        gameboard.keyCode = 39; // Right
        gameboard.keyPressed();
        assertTrue(player.getKeyRight());
        gameboard.keyReleased();
        assertFalse(player.getKeyRight());

        gameboard.keyCode = 40; // Down
        gameboard.keyPressed();
        assertTrue(player.getKeyDown());
        gameboard.keyReleased();
        assertFalse(player.getKeyDown());

        gameboard.draw();    
        // check win/lose
        gameboard.setLives(1);
        gameboard.modifyLife(-1);
        assertTrue(gameboard.isGameOver());
        
        gameboard.setGameOver(false);

        for (TileObject tile : gameboard.allTileObjects()) {
            if (tile.isSoil()) tile.setGrass();
        }
        // switch level
        gameboard.updateScore();
        assertTrue(gameboard.getCurrentLevel() == gameboard.getLevelList().get(1));
    }

    @Test 
    public void testParsing() {
        gameboard.getLevelList().clear();
        gameboard.getEnemies().clear();
        // parse JSON
        gameboard.parseJSON("test_config.json");
        assertTrue(gameboard.getLives()==1);
        assertTrue(gameboard.getLevelList().size()==1);
        Level level = gameboard.getLevelList().get(0);
        assertTrue(level.getGoal()==50.0);
        assertTrue(level.getOutlay().equals("level1.txt"));
        assertTrue(level.getPowerupType().equals("ZAWARUDO"));
        assertTrue(level.getEnemyList().size()==2);
        
        // manually parse level
        // check parseEnemies
        gameboard.parseEnemies(level);
        assertTrue(gameboard.getEnemies().size()==1);
        Enemy enemy = gameboard.getEnemies().get(0);
        assertEquals(gameboard.getSprites().get("worm"), enemy.getSprite());
        assertTrue(enemy.getTile(gameboard.allTileObjects()).isSoil());
    }

}
