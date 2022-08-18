package lawnlayer;

import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

import java.util.*;

public class AgentTest {

    Agent agent;
    App gameboard;
    TileObject[][] map;
    TileObject[] allTileObjects;
    TileObject agentTile;
    HashMap<Integer,TileObject> surroundingTiles;

    @BeforeEach
    public void setUp() {
        agent = new Agent(100,100);
    }

    @Test
    public void testConstructor() {
        assertEquals(110, agent.getX());
        assertEquals(110, agent.getY());
        assertEquals("moveUp", agent.movementIdx.get(0));
        assertEquals("moveRight", agent.movementIdx.get(1));
        assertEquals("moveDown", agent.movementIdx.get(2));
        assertEquals("moveLeft", agent.movementIdx.get(3));
        for (boolean movement : agent.movements.values()) {
            assertFalse(movement);
        }
    }

    @Test
    public void testMovement() {
        agent.movements.put("moveUp", true);
        agent.tick();
        assertEquals(110, agent.getX());
        assertEquals(108, agent.getY());
        
        agent.movements.put("moveRight", true);
        agent.tick();
        assertEquals(112, agent.getX());
        assertEquals(106, agent.getY());
        
        agent.movements.put("moveDown", true);
        agent.tick();
        assertEquals(114, agent.getX());
        assertEquals(106, agent.getY());
        
        agent.movements.put("moveLeft", true);
        agent.tick();
        assertEquals(114, agent.getX());
        assertEquals(106, agent.getY());
    }

    @Test
    public void testSnapBoundary() {
        // 1280 x 720 (Y exclude TOPBAR)
        agent.setX(1500);
        agent.setY(800);
        agent.snapBoundary(agent.getX(), agent.getY());
        assertEquals(1270, agent.getX());
        assertEquals(710, agent.getY());

        agent.setX(-10);
        agent.setY(30);
        agent.snapBoundary(agent.getX(), agent.getY());
        assertEquals(10, agent.getX());
        assertEquals(90, agent.getY());        
    }

    @Test
    public void testGetAgentTile() {
        // tile row 1 col 1
        agent.setX(30);
        agent.setY(110);
        
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        
        map = gameboard.parseTiles("level1.txt");
        allTileObjects = gameboard.allTileObjects();       

        agentTile = agent.getTile(allTileObjects);
        assertEquals(map[1][1], agentTile);
    }

    @Test
    public void testGetSurroundingTiles() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        
        map = gameboard.parseTiles("level1.txt");
        agentTile = map[1][1]; // agent tile row 1 column 1

        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertEquals(map[0][0], surroundingTiles.get(1));
        assertEquals(map[0][1], surroundingTiles.get(2));
        assertEquals(map[0][2], surroundingTiles.get(3));
        assertEquals(map[1][0], surroundingTiles.get(4));
        assertEquals(map[1][2], surroundingTiles.get(5));
        assertEquals(map[2][0], surroundingTiles.get(6));
        assertEquals(map[2][1], surroundingTiles.get(7));
        assertEquals(map[2][2], surroundingTiles.get(8));

        // border surrounding
        // left wall - not corner
        agentTile = map[15][0];
        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertTrue(surroundingTiles.size()==5);
        assertEquals(map[14][0], surroundingTiles.get(2));
        assertEquals(map[14][1], surroundingTiles.get(3));
        assertEquals(map[15][1], surroundingTiles.get(5));
        assertEquals(map[16][0], surroundingTiles.get(7));
        assertEquals(map[16][1], surroundingTiles.get(8));
        
        // left wall - top left corner
        agentTile = map[0][0];
        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertTrue(surroundingTiles.size()==3);
        assertEquals(map[0][1], surroundingTiles.get(5));
        assertEquals(map[1][0], surroundingTiles.get(7));
        assertEquals(map[1][1], surroundingTiles.get(8));
        
        // left wall - bottom left corner
        agentTile = map[31][0];
        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertTrue(surroundingTiles.size()==3);
        assertEquals(map[30][0], surroundingTiles.get(2));
        assertEquals(map[30][1], surroundingTiles.get(3));
        assertEquals(map[31][1], surroundingTiles.get(5));

        // Right wall - not corners
        agentTile = map[15][63];
        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertTrue(surroundingTiles.size()==5);
        assertEquals(map[14][62], surroundingTiles.get(1));
        assertEquals(map[14][63], surroundingTiles.get(2));
        assertEquals(map[15][62], surroundingTiles.get(4));
        assertEquals(map[16][62], surroundingTiles.get(6));
        assertEquals(map[16][63], surroundingTiles.get(7));

        // Right wall - top right corner
        agentTile = map[0][63];
        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertTrue(surroundingTiles.size()==3);
        assertEquals(map[0][62], surroundingTiles.get(4));
        assertEquals(map[1][62], surroundingTiles.get(6));
        assertEquals(map[1][63], surroundingTiles.get(7));
        
        // Right wall - bottom right corner
        agentTile = map[31][63];
        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertTrue(surroundingTiles.size()==3);
        assertEquals(map[30][62], surroundingTiles.get(1));
        assertEquals(map[30][63], surroundingTiles.get(2));
        assertEquals(map[31][62], surroundingTiles.get(4));
    
        // top wall - not corners
        agentTile = map[0][30];
        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertTrue(surroundingTiles.size()==5);
        assertEquals(map[0][29], surroundingTiles.get(4));
        assertEquals(map[0][31], surroundingTiles.get(5));
        assertEquals(map[1][29], surroundingTiles.get(6));
        assertEquals(map[1][30], surroundingTiles.get(7));
        assertEquals(map[1][31], surroundingTiles.get(8));

        // bottom wall - not corners
        agentTile = map[31][30];
        surroundingTiles = agent.getSurroundingTiles(map, agentTile);
        assertTrue(surroundingTiles.size()==5);
        assertEquals(map[30][29], surroundingTiles.get(1));
        assertEquals(map[30][30], surroundingTiles.get(2));
        assertEquals(map[30][31], surroundingTiles.get(3));
        assertEquals(map[31][29], surroundingTiles.get(4));
        assertEquals(map[31][31], surroundingTiles.get(5));


    }

}
