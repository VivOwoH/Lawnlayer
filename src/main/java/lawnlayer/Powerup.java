package lawnlayer;

/**
 * Represents the powerup collectible in the game.
 */
public class Powerup extends Agent {
    
    private String powerupKey;
    private static final String[] POWERUP_KEYS = {"Invincible", "ZAWARUDO"};

    /**
     * Creates a new powerup with specified (x,y) coordinates.
     * @param x x-coordiate
     * @param y y-coordiate
     */
    public Powerup(int x, int y) {
        super(x,y);
    }

    /**
     * Gets all available powerup types.
     * @return the string array of all available powerup types for this game instance
     */
    public static String[] POWERUP_KEYS() {
        return POWERUP_KEYS;
    }

    /**
     * Sets the type of this powerup object.
     * @param key the specified powerup type string
     */
    public void setPowerupKey(String key) {
        this.powerupKey = key;
    }

    /**
     * Gets the type of this powerup object.
     * @return the type string of this powerup object
     */
    public String getPowerupKey() {
        return this.powerupKey;
    }

}
