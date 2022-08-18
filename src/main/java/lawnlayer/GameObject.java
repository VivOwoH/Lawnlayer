package lawnlayer;

import processing.core.PImage;
import processing.core.PApplet;

/**
 * The base class for all game objects.
 */
public class GameObject {
    
    // spawning coordinates
    private int x;
    private int y;

    // sprite
    private PImage sprite;
    private boolean spriteSet = false;

    /**
     * Create a game object with specified (x,y) coordinates.
     * The coordinates are set as the center of a tile, instead of top-left.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public GameObject(int x, int y) {
        this.x = x + App.SPRITESIZE()/2;
        this.y = y + App.SPRITESIZE()/2;
    }

    /**
     * Sets the sprite for this game object.
     * @param sprite the {@link PImage} sprite 
     */
    public void setSprite(PImage sprite) {
        this.sprite = sprite;
        this.spriteSet = true;
    }

    /**
     * Gets the sprite for this game object.
     * @return the {@link PImage} sprite 
     */
    public PImage getSprite() {
        return this.sprite;
    }

    /**
     * Checks if a sprite is set for this game object.
     * @return <code>true</code> if set, otherwise <code>false</code>
     */
    public boolean isSpriteSet() {
        return this.spriteSet;
    }

    /**
     * Removes the sprite for this game object.
     */
    public void removeSprite() {
        this.sprite = null;
        this.spriteSet = false;
    }

    /**
     * Draws the sprite of this game object on screen. The sprite is always drawn from  
     * the top-left corner of tile.
     * <p> x coordinate = x - {@link lawnlayer.App#SPRITESIZE()} / 2<br> 
     * y coordinate = y - {@link lawnlayer.App#SPRITESIZE()} / 2
     * @param app the base class app of {@link PApplet}
     */
    public void draw(PApplet app) {
        // System.out.println(this.sprite);
        app.image(this.sprite, this.x-App.SPRITESIZE()/2, this.y-App.SPRITESIZE()/2);
    }

    /**
     * Gets the x-coordinate of this game object.
     * @return x-coordinate as an integer
     */
    public int getX() {
        return this.x;
    }

    /**
     * Gets the y-coordinate of this game object.
     * @return y-coordinate as an integer
     */
    public int getY() {
        return this.y;
    }

    /**
     * Sets the x-coordinate of this game object.
     * @param x the integer x-coordinate to be set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of this game object.
     * @param y the integer y-coordinate to be set
     */
    public void setY(int y) {
        this.y = y;
    }
}
