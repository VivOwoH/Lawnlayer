package lawnlayer;

import java.util.Map;
import processing.core.PImage;

/**
 * Represents a tile object in the tilemap.
 */
public class TileObject extends GameObject{

    // grids
    private int row;
    private int col;

    // tile types
    private boolean concrete = false;
    private boolean grass = false;
    private boolean path = false;
    private boolean pathHit = false;
    private boolean soil = true;

    /**
     * Creates a new tile object at coordinates calculated from specified
     * row and column.
     * @param row the row of this tile in the whole tilemap
     * @param col the column of this tile in the whole tilemap
     */
    public TileObject(int row, int col) {
        // x-coor = col; y-coor = row
        super(col*App.SPRITESIZE(), App.TOPBAR()+row*App.SPRITESIZE());
        this.row = row;
        this.col = col;
    }

    /**
     * Get the row of this tile.
     * @return the row as an integer
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Get the column of this tile.
     * @return the column as an integer
     */
    public int getCol() {
        return this.col;
    }

    /**
     * Checks if this tile is a soil.
     * @return <code>true</code> if soil tile, otherwise <code>false</code>.
     */
    public boolean isSoil() {
        return this.soil;
    }

    /**
     * Sets this tile to a soil tile. Resets other tile types.
     */
    public void setSoil() {
        this.soil = true;
        // set other types to false to avoid bug
        this.concrete = false;
        this.grass = false;
        this.path = false;
        this.pathHit = false;
    }

    /**
     * Checks if this tile is a concrete.
     * @return <code>true</code> if concrete tile, otherwise <code>false</code>.
     */
    public boolean isConcrete() {
        return this.concrete;
    }

    /**
     * Sets this tile to a concrete tile. Resets other tile types.
     */
    public void setConcrete() {
        this.concrete = true; 
        // set other types to false to avoid bug
        this.grass = false;
        this.path = false;
        this.pathHit = false;
        this.soil = false;
    }

    /**
     * Checks if this tile is a grass.
     * @return <code>true</code> if grass tile, otherwise <code>false</code>.
     */
    public boolean isGrass() {
        return this.grass;
    }

    /**
     * Sets this tile to a grass tile. Resets other tile types.
     */
    public void setGrass() {
        this.grass = true;
        // set other types to false to avoid bug
        this.concrete = false;
        this.path = false;
        this.pathHit = false;
        this.soil = false;
    }

    /**
     * Checks if this tile is a path.
     * @return <code>true</code> if path tile, otherwise <code>false</code>.
     */
    public boolean isPath() {
        return this.path;
    }

    /**
     * Sets this tile to a path tile. Resets other tile types.
     */
    public void setPath() {
        this.path = true;
        // set other types to false to avoid bug
        this.concrete = false;
        this.grass = false;
        this.soil = false;
        this.pathHit = false;
    }

    /**
     * Checks if this tile is a red path.
     * @return <code>true</code> if red path tile, otherwise <code>false</code>.
     */
    public boolean isPathHit() {
        return this.pathHit;
    }

    /**
     * Sets this tile to a red path tile. Resets other tile types.
     */
    public void setPathHit() {
        this.pathHit = true;
    }

    /**
     * Updates tile sprite when the tile changes type.
     * @param sprites   all loaded sprites in a <code>Map</code> accessible by 
     *                  a string key
     */
    public void updateTileSprite(Map<String,PImage> sprites) {
        // update needed: grass, path, soil
        if (this.isGrass()) {
            this.setSprite(sprites.get("grass"));
        }
        else if (this.isPath()) {
            // hit path = red
            if (this.isPathHit())
                this.setSprite(sprites.get("hitPath"));
            // normal path = green
            else
                this.setSprite(sprites.get("path"));
        } 
        else if (this.isSoil()) {
            this.removeSprite();
        }   
    }

    // public String toString() {
    //     return Integer.toString(this.row) +" "+ Integer.toString(this.col);
    // }

}
