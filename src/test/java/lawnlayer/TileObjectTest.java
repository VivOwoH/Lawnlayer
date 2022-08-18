package lawnlayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import processing.core.PImage;
import processing.core.PApplet;
import java.util.*;

public class TileObjectTest {
    
    App gameboard;
    TileObject tile;
    Map<String, PImage> sprites;
    

    @Test
    public void testUpdateTileSprite() {
        gameboard = new App();
        gameboard.noLoop();
        // Tell PApplet to create the worker threads for the program
        PApplet.runSketch(new String[] {"App"}, gameboard);
        gameboard.setup();
        gameboard.delay(1000); //to give time to initialise stuff before drawing begins
        sprites = gameboard.getSprites();

        tile = new TileObject(1, 1);
        assertFalse(tile.isSpriteSet());
        tile.setGrass();
        tile.updateTileSprite(sprites);
        assertTrue(tile.isSpriteSet());
        assertEquals(sprites.get("grass"), tile.getSprite());

        tile.setPath();
        tile.updateTileSprite(sprites);
        assertEquals(sprites.get("path"), tile.getSprite());

        tile.setPathHit();
        tile.updateTileSprite(sprites);
        assertEquals(sprites.get("hitPath"), tile.getSprite());

        tile.setSoil();
        tile.updateTileSprite(sprites);
        assertNull(tile.getSprite());
        assertFalse(tile.isSpriteSet());
    }
}
