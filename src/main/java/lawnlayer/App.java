/**
 * @author Vivian Ha (weha7612@uni.sydney.edu.au)
 */
package lawnlayer;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;
import processing.data.JSONObject;
import processing.data.JSONArray;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;

/**
 * Base class for the game. Represents the gameboard that has general control and 
 * stores all attributes and objects of this game instance.
 */
public class App extends PApplet {

    // window size 1280 x 720px
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    // sprite 20 x 20px
    private static final int SPRITESIZE = 20;
    private static final int TOPBAR = 80;
    // Grid is 64col x 32row
    private static final int GRID_COL = WIDTH / SPRITESIZE;
    private static final int GRID_ROW = (HEIGHT - TOPBAR) / SPRITESIZE; 

    private static final int FPS = 60; 

    // map
    private TileObject[][] tileMap;

    // game and level data
    private List<Level> levelList;
    private Level currentLevel;
    private int lives;
    private double score;

    private Player player;
    private List<Enemy> allEnemies;
    private Powerup powerup; // only 1 powerup at a time present on screen
    private boolean powerupInEffect;
    private int defaultTimer = 10; // duration and delay
	private int powerupTimer;
    private String powerupKeyText;
    private int delayInterval;
    private String configPath;
    private String timestopSfxPath;
    private String invincibleSfxPath;

    private Map<String,PImage> sprites;
    
    private int colorWheel = 0;
    private int brightnessWheel = 255;

    private boolean gameOver;

    // -------------- CONSTRUCTOR -----------------
    /**
     * App class constructor. Creates a new app (i.e.game instance).
     */
    public App() {
        this.configPath = "config.json";
        this.tileMap = new TileObject[App.GRID_ROW()][App.GRID_COL()]; 
        this.levelList = new ArrayList<Level>();
        this.allEnemies = new ArrayList<Enemy>();

        // delay interval random between 1~10s (60~600 frames) for 60FPS
        this.delayInterval = (new Random().nextInt(this.defaultTimer) + 1) * App.FPS;
    }

    // -------------- GETTER/SETTER -----------------
    /**
     * Gets the constant size that all sprites would be sketched in for this game.
     * @return the sprite size as an integer
     */
    public static int SPRITESIZE() {
        return SPRITESIZE;
    }

    /**
     * Gets the number of rows in the grid map. Game area is defined as a grid-based 
     * tile map.
     * @return the number of rows in the grid map
     */
    public static int GRID_ROW() {
        return GRID_ROW;
    }

    /**
     * Gets the number of columns in the grid map.
     * @return the number of columns in the grid map
     */
    public static int GRID_COL() {
        return GRID_COL;
    }

    /**
     * Gets the size of top bar where game information (lives,goal,level etc.) is displayed.
     * @return the vertical length of the top bar
     */
    public static int TOPBAR() {
        return TOPBAR;
    }

    /**
     * Gets the FPS of this app.
     * @return integer FPS value
     */
    public static int FPS() {
        return FPS;
    }

    /**
     * Gets the list of all levels for this game instance.
     * @return the list of all {@link Level} 
     */
    public List<Level> getLevelList() {
        return this.levelList;
    }

    /**
     * Gets the reference to the current level.
     * @return the current {@link Level}
     */
    public Level getCurrentLevel() {
        return this.currentLevel;
    }

    /**
     * Sets the number of lives in this game instance
     * @param lives the integer number of lives to be updated
     */
    public void setLives(int lives) {
        this.lives = lives;
    }

    /**
     * Gets the numebr of lives in this game instance.
     * @return the integer number of lives
     */
    public int getLives() {
        return this.lives;
    }

    /**
     * Gets the reference to the current player in this game instance.
     * @return the player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the current tilemap of this level.
     * @return the 2d tilemap
     */
    public TileObject[][] getTileMap() {
        return this.tileMap;
    } 

    /**
     * Gets all the tile objects of this level.
     * @return all tile objects in a single array
     */
    public TileObject[] allTileObjects() {
        ArrayList<TileObject> tmp = new ArrayList<TileObject>();
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                tmp.add(tileMap[i][j]);
            }
        }
        return tmp.toArray(new TileObject[0]);
    }

    /**
     * Gets all enemies of this level.
     * @return all enemies in a single list
     */
    public List<Enemy> getEnemies() {
        return this.allEnemies;
    }

    /**
     * Gets all sprites loaded for this game.
     * @return the {@link PImage} sprites in a map with string keys
     */
    public Map<String,PImage> getSprites() {
        return this.sprites;
    }

    /**
     * Gets the powerup collectible.
     * @return the {@link Powerup} object
     */
    public Powerup getPowerup() {
        return this.powerup;
    }

    public void setDelayInterval(int value) {
        this.delayInterval = value;
    } 

    public int getDelayInterval() {
        return this.delayInterval;
    }

    public void setPowerupInEffect(boolean value) {
        this.powerupInEffect = value;
    }

    public boolean isPowerupInEffect() {
        return this.powerupInEffect;
    }

    public void setPowerupTimer(int value) {
        this.powerupTimer = value;
    }

    public int getPowerupTimer() {
        return this.powerupTimer;
    }

    public void setGameOver(boolean value) {
        this.gameOver = value;
    }

    public boolean isGameOver() {
        return this.gameOver;
    }

    //------------------- Setting up ----------------------------
    /**
     * Initialises the setting of the window size
     */
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * 
     * Load all resources such as images, JSON data (See {@link #parseJSON(String)}).
     * Called once when the program starts.
     */
    public void setup() {
        
        frameRate(FPS);

        this.sprites = new HashMap<String,PImage>();
        // Load sprites during setup
        this.sprites.put("grass", loadImage(this.getClass().getResource("grass.png")
                            .getPath().replace("%20"," "))); 
        this.sprites.put("concrete", loadImage(this.getClass().getResource("concrete_tile.png")
                            .getPath().replace("%20"," ")));      
        this.sprites.put("beetle", loadImage(this.getClass().getResource("beetle.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("worm", loadImage(this.getClass().getResource("worm.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("player", loadImage(this.getClass().getResource("ball.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("path", loadImage(this.getClass().getResource("path.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("hitPath", loadImage(this.getClass().getResource("hitPath.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("invincible", loadImage(this.getClass().getResource("power.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("rainbowPlayer", loadImage(this.getClass().getResource("rainbow.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("enemyClown", loadImage(this.getClass().getResource("clown.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("stopTime", loadImage(this.getClass().getResource("time.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("enemyFrozen", loadImage(this.getClass().getResource("frozen.png")
                            .getPath().replace("%20"," ")));
        this.sprites.put("win", loadImage(this.getClass().getResource("win.jpg")
                            .getPath().replace("%20"," ")));
        this.sprites.put("lose", loadImage(this.getClass().getResource("lose.jpg")
                            .getPath().replace("%20"," ")));

        
        // Load sound effects files
        this.timestopSfxPath = this.getClass().getResource("zawarudo.wav")
                            .getPath().replace("%20"," ");
        this.invincibleSfxPath = this.getClass().getResource("rick.wav")
                            .getPath().replace("%20"," ");
        

        // Parse JSON config file
        parseJSON(this.configPath);
        // Parse first level
        this.parseLevel(this.levelList.get(0));
    }

    // -------------- Gameboard control -----------------
    /**
     * Modifys the number of remaining lives in this game instance.
     * @param n integer to be added to the remaining lives
     *          (negative for decrementation)
     */
    public void modifyLife(int n) {
        this.lives += n;
        checkWinLose();
    }

    /**
     * Updates the current score of this level. The score is calculated as
     * (filled grass objects / all soil tile objects) * 100 in percentage.
     * This percentage is rounded up.
     */
    public void updateScore() {
        TileObject[] tiles = this.allTileObjects();
        int numOfTiles = tiles.length;
        int numOfGrass = 0;
        for (TileObject tile : tiles) {
            if (tile.isConcrete()) numOfTiles--;
            if (tile.isGrass()) numOfGrass++;
        }
        // System.out.printf("num of grass:%d, num of Tiles:%d%n",numOfGrass,numOfTiles);
        this.score = Math.ceil((double)numOfGrass / (double)numOfTiles * 100.0);
        checkWinLose();
    }

    /**
     * Checks if the game is won or lost. 
     * This function is called either when score is updated ({@link #updateScore()}), 
     * or when lives changed ({@link #modifyLife(int)}).
     * Won if goal reached. If more levels to come, call upon a new level. 
     * Otherwise, display a win screen. Lost if no lives left. Display a lose screen.
     * See {@link #parseLevel(Level)} and {@link #displayScreen(boolean, String)}.
     */
    public void checkWinLose() {
        // win
        if (this.score >= this.currentLevel.getGoal()) {
            int curLvIdx = this.levelList.indexOf(this.currentLevel);
            if (curLvIdx != this.levelList.size()-1)
                this.parseLevel(this.levelList.get(curLvIdx+1)); // parse next level
            else
                displayScreen(true, "You win"); // display a win screen
        }
        // lose 
        else if (this.lives <= 0) {
            displayScreen(false, "Game over"); // display a lose screen
        }
    }

    /**
     * Displays a screen overlay when game finishes. The game stops all executions.
     * @param win   <code>true</code> if game is won, 
     *              <code>false</code> if game is lost
     * @param text  text to display on screen
     */
    public void displayScreen(boolean win, String text) {
        this.gameOver = true; 
        if (win) {
            image(sprites.get("win"), 320, 180);
            textAlign(CENTER);
            textSize(50);
            text(text, 580, 300);
        }   
        else {
            image(sprites.get("lose"), 320, 180);
            textAlign(CENTER);
            textSize(50);
            text(text, 640, 380);
        }
        stop();
    }

    /**
     * Nullifys the current player reference. Spawn in a new player at the top 
     * left of the tilemap.
     */
    public void newPlayer() {
        this.player = null;
        this.player = new Player(0, TOPBAR);
        this.player.setSprite(sprites.get("player"));
        // *Edge case: player release when player die, resulting NullPointerError
        this.player.tick(this); // initial update to prevent error
    }


    // -------------- Parsing ----------------------------

    /**
     * Parses level and sets current level. Objects to be parsed in the level includes:
     * tilemap ({@link #parseTiles(String)}), enemies ({@link #parseEnemies(Level)}),
     * player ({@link #newPlayer()}).
     * @param level the level to be parsed
     */
    public void parseLevel(Level level) {
        // clear everything
        this.score = 0.0; // reset score
        this.allEnemies.clear();
        this.powerupInEffect = false;

        parseTiles(level.getOutlay()); // tilemap
        parseEnemies(level); // enemies
        newPlayer(); // player
        this.currentLevel = level;
    }

    /**
     * Parses config JSON file to get a list of levels. 
     * See also {@link processing.data.JSONObject}, {@link processing.data.JSONArray}.
     * @param path the path of config file
     */ 
    public void parseJSON(String path) {
        // getting data from JSON
        JSONObject data = loadJSONObject(path);  

        // lives (not specific to levels)
        this.lives = data.getInt("lives");

        // levels data
        JSONArray levels = data.getJSONArray("levels");
        for (int i = 0; i < levels.size(); i++) {
            JSONObject level = levels.getJSONObject(i);
            String outlay = level.getString("outlay");
            String powerup = level.getString("powerup");
            double goal = level.getDouble("goal");
            
            // instantiate a new level 
            Level newLevel = new Level(outlay, powerup, goal);

            // get enemies data in this level
            JSONArray enemies = level.getJSONArray("enemies");
            for (int j = 0; j < enemies.size(); j++) {
                JSONObject enemy = enemies.getJSONObject(j);

                HashMap<String,Object> enemyData = new HashMap<String,Object>();
                // type data 0:"worm", 1:"beetle"
                enemyData.put("type", enemy.getInt("type"));
                // spawn position "random" or coordinates "row,col" 
                enemyData.put("spawn", enemy.getString("spawn")); 

                newLevel.addEnemy(enemyData);
            }
            this.levelList.add(newLevel); // append this level to level list
        }
    }

    /**
     * Parse level.txt file to update tilemap
     * @param filename                  the file that contains level map 
     * @throws IllegalArgumentException if invalid map
     * @return the 2d tilemap
     */
    public TileObject[][] parseTiles (String filename) throws IllegalArgumentException {
        try {
            File f = new File(filename);
            Scanner scan = new Scanner(f);
            
            int i = 0;
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                for (int j = 0; j < line.length(); j++) {
                    TileObject tmp = new TileObject(i,j);
                    
                    if (String.valueOf(line.charAt(j)).equals("X")) {
                        // marked concrete
                        tmp.setSprite(sprites.get("concrete"));
                        tmp.setConcrete();
                    }
                    tileMap[i][j] = tmp;
                }
                i++;
            }
            scan.close();
            // Checking invalid map (first/last row, first/last col must be concrete)
            for (int n = 0; n < tileMap[0].length; n++) {
                if (!tileMap[0][n].isConcrete()) 
                    throw new IllegalArgumentException("first row invalid");
                if (!tileMap[GRID_ROW-1][n].isConcrete())
                    throw new IllegalArgumentException("last row invalid");
            }
            for (int m = 0; m < tileMap.length; m++) {
                if (!tileMap[m][0].isConcrete())
                    throw new IllegalArgumentException("first column invalid");
                if (!tileMap[m][0].isConcrete())
                    throw new IllegalArgumentException("last column invalid");
            }   
        } catch (FileNotFoundException e) {
            System.err.println("Level data file not found!");
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
        return tileMap;
    }

    /**
     * Parse enemy data to spawn enemies and update enemy list
     * @param level                     the level to get the data from
     * @throws IllegalArgumentException if spawn too near edges of game area 
     *                                  (keep 1 block away from walls)
     */
    public void parseEnemies(Level level) throws IllegalArgumentException {
        for (HashMap<String,Object> enemy : level.getEnemyList()) {
            // we want a tile as spawn position of this enemy
            TileObject tile = null;

            if (enemy.get("spawn").toString().equals("random")) {
                // random spawn tile in soil area
                while (true) {
                    int rndTileRow = new Random().nextInt(tileMap.length-4) + 2; //2~29
                    int rndTileCol = new Random().nextInt(tileMap[0].length-4) + 2; //2~61
                    tile = tileMap[rndTileRow][rndTileCol];
                    if (tile.isSoil())
                        break; // tile empty, no need to generate another random tile
                }
            } else { 
                /* SHOULD BE fixed spawn tile (given "rol,col")
                   check with NumberFormatException */
                try {
                    String string = enemy.get("spawn").toString();
                    String[] parts = string.split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    if (row < 2 || row > tileMap.length-3 || col < 2 || col > tileMap[0].length-3)
                        throw new IllegalArgumentException("Spawn position too close to walls");
                    tile = tileMap[row][col];
                } catch (NumberFormatException e) {
                    System.err.println("Invalid spawn position.");
                } catch (IllegalArgumentException e) {
                    System.err.println(e);
                    continue;
                }
            }

            // get random pixel location within the chosen tile
            int xMin = tile.getX() - SPRITESIZE/2;
            int xMax = tile.getX() + SPRITESIZE/2;
            int yMin = tile.getY() - SPRITESIZE/2;
            int yMax = tile.getY() + SPRITESIZE/2;
            int rndX = new Random().nextInt((xMax+1) - xMin) + xMin;
            int rndY = new Random().nextInt((yMax+1) - yMin) + yMin;

            if ((int)enemy.get("type") == 0) {
                // type "0" = worm
                Enemy worm = new Enemy(rndX, rndY);
                worm.setSprite(sprites.get("worm"));
                this.allEnemies.add(worm);
            } else if ((int)enemy.get("type") == 1) {
                // type "0" = beetle
                Enemy beetle = new Beetle(rndX, rndY);
                beetle.setSprite(sprites.get("beetle"));
                this.allEnemies.add(beetle);
            }
        }
    }

    /**
     * Parses powerup sprite for this level. The type of powerup is based on the 
     * key string obtained from the config file when the config file was parsed.
     * @param level the level to get the data from
     */
    public void parsePowerup(Level level) {
         // random spawn tile in soil area
         TileObject tile = null;
         while (true) {
             int rndTileRow = new Random().nextInt(tileMap.length-3) + 1; //1~30
             int rndTileCol = new Random().nextInt(tileMap[0].length-3) + 1; //1~62
             tile = tileMap[rndTileRow][rndTileCol];
             if (tile.isSoil())
                 break; // tile empty, no need to generate another random tile
         }
        // spawn a new Powerup into the random tile
        this.powerup = new Powerup(tile.getX()-SPRITESIZE/2, tile.getY()-SPRITESIZE/2);

        // choose powerup sprite
        String key = level.getPowerupType();
        if (key.equals("random"))
            key = Powerup.POWERUP_KEYS()[new Random()
                            .nextInt(Powerup.POWERUP_KEYS().length)];
            this.powerup.setPowerupKey(key);

        if (this.powerup.getPowerupKey().equals("Invincible")) {
            this.powerup.setSprite(sprites.get("invincible"));
        }
        else if (this.powerup.getPowerupKey().equals("ZAWARUDO")) {
            this.powerup.setSprite(sprites.get("stopTime"));
        }
    }

    // ----------------- Updating obejects -----------------------

    /**
     * Updates the state of powerup. If last powerup still in effect, decrement 
     * duration timer. Otherwise, restore states and sprites of agents (enemies 
     * and player).
     * <p> 1. Powerup present in game:
     * If player consumes powerup: generate effect (See {@link lawnlayer.Enemy#freeze()}, 
     *                               {@link lawnlayer.Enemy#setVulnerable(boolean)}); 
     *                               set sprites (See {@link lawnlayer.GameObject#setSprite(PImage)}); 
     *                               play sound effect (See {@link #playSound(String)}).
     * If grass area swallows powerup: remove without effect.
     * <p> 2. Powerup not present in game:
     * If last powerup still in effect: <code>return</code>
     * If no in-effect powerup: start decrementing delay interval timer
     * If delay timer finish: generate another random delay timer; 
     *                          spawn in a new powerup ({@link #parsePowerup(Level)}).
     */
    public void updatePowerup() {
        // Update state of powerup
        if (this.powerupInEffect)
            this.powerupTimer--; // decrement powerup timer
        
        // powerup faded, make enemies and player normal
        if (this.powerupInEffect && this.powerupTimer <= 0) {
            this.powerupInEffect = false;
            this.colorWheel = 0;
            this.brightnessWheel = 255;
            this.player.setSprite(sprites.get("player"));
            for (Enemy enemy : this.allEnemies) {
                // restore normal sprites
                if (enemy instanceof Beetle) 
                    enemy.setSprite(sprites.get("beetle"));
                else 
                    enemy.setSprite(sprites.get("worm"));
                // restore previous state
                if (this.powerupKeyText.equals("Invincible")) 
                    enemy.setVulnerable(false);
                else if (this.powerupKeyText.equals("ZAWARUDO")) 
                    enemy.unfreeze();
            }
        }
        
        // Check state of powerup
        if (this.powerup != null) {
            TileObject powerupTile = this.powerup.getTile(this.allTileObjects());
            // player consumes powerup
            if (powerupTile == this.player.getPlayerTile()) {
                // default*FPS = 10*60 = 600 frames
                this.powerupTimer = this.defaultTimer * FPS; // start the timer
                if (this.powerup.getPowerupKey().equals("Invincible")) {
                    // play sound effect
                    try {
                        this.playSound(invincibleSfxPath);
                    } catch (Exception e) {
                        System.out.println("Sfx error");
                    }
                    this.player.setSprite(sprites.get("rainbowPlayer")); // set player sprite
                    // set enemy sprite
                    for (Enemy enemy : this.allEnemies) {
                        enemy.setSprite(sprites.get("enemyClown"));
                        enemy.setVulnerable(true);
                    }
                } 
                else if (this.powerup.getPowerupKey().equals("ZAWARUDO")) {
                    // play sound effect
                    try {
                        this.playSound(timestopSfxPath);
                    } catch (Exception e) {
                        System.out.println("Sfx error");
                    }
                    // set enemy sprite
                    for (Enemy enemy : this.allEnemies) {
                        enemy.setSprite(sprites.get("enemyFrozen"));
                        enemy.freeze();
                    } 
                }
                this.powerupInEffect = true;
                // remember its key before removing the powerup object
                this.powerupKeyText = this.powerup.getPowerupKey();
                this.powerup = null;
            }
            // Remove powerup if swallowed by grass
            if (powerupTile.isGrass())
                this.powerup = null;
        }
        else { // no powerup on screen
            // 1. Last powerup still in effect -> do nothing
            // 2. No powerup in effect -> start decrementing delay timer
            // 3. Delay timer finish -> generate another random interval, 
            //                          spawn in a new powerup
            if (this.powerupInEffect) return;
            if (this.delayInterval > 0) {
                this.delayInterval--;
            }
            else {
                this.delayInterval = (new Random().nextInt(this.defaultTimer) + 1) * FPS;
                this.parsePowerup(this.currentLevel); // parse in a new powerup
            } 
        }
    }


    /**
     * Updates all texts on screen.
     */
    public void updateTexts() {
        // Font data
        String fontFile = this.getClass().getResource("PressStart2P.ttf").getPath()
                                                .replace("%20"," ");
        PFont press = createFont(fontFile, 28);
        textFont(press);
        textAlign(CENTER);

        // Lives   
        text("Lives:" + Integer.toString(this.lives), 160, 50);
        // Score
        text(Integer.toString((int)this.score) + "%/" + 
             Integer.toString((int)this.currentLevel.getGoal()) + "%", 920, 50);
        // Level number
        textSize(18);
        text("Level " + Integer.toString(this.levelList.indexOf(this.currentLevel)+1), 
                                                                    1140, 60);
        // Powerup timer
        if (this.powerupInEffect) {
            textSize(20);
            text(this.powerupKeyText + ": " + 
                    Integer.toString((int)this.powerupTimer/60), 640, 70);
        }
    }

    /**
     * Draw all elements in the game by current frame [called automatically, 
     * should never be called explicitly. This should be controlled with 
     * {@link PApplet#noLoop()}, {@link PApplet#redraw()} and {@link PApplet#loop()}]
     */
    public void draw() {
        // long start = System.currentTimeMillis();

        if (this.powerupInEffect) {
            colorMode(HSB);
            if (this.powerupKeyText.equals("Invincible")){
                background(this.colorWheel++, 200, 100);
                this.colorWheel = this.colorWheel % 256;
            } else if (this.powerupKeyText.equals("ZAWARUDO")){
                // bright -> dark blue
                background(150, 200, this.brightnessWheel--);
                this.brightnessWheel = Math.max(this.brightnessWheel,60);
            }
        } else {
            colorMode(RGB);
            // brown background
            background(101,67,33);
        }

        // update texts
        updateTexts();

        // update and draw tile map by frames
        for (TileObject tile : this.allTileObjects()) {
            tile.updateTileSprite(sprites);
            if (tile.isSpriteSet()) 
                tile.draw(this);
        }

        // update and draw all agents (player and enemies) by frames
        for (Enemy enemy : this.getEnemies()) {
            enemy.tick(this);
            enemy.draw(this);
        }
        this.getPlayer().tick(this);
        this.getPlayer().draw(this);

        // update and draw the powerup
        this.updatePowerup();
        if (this.powerup != null)
            this.powerup.draw(this);

        // // check FPS
        // long timeTaken = System.currentTimeMillis()-start; 
        // if (timeTaken > 16) System.out.printf("Slow FPS: %d%n",timeTaken);
    }


    // --------------- Key Listener ------------------
    /**
     * Called every frame to detect if a key is down.
     * @see lawnlayer.Player#pressUp()
     * @see lawnlayer.Player#pressRight()
     * @see lawnlayer.Player#pressDown()
     * @see lawnlayer.Player#pressLeft()
     */
    public void keyPressed() {
        // Left: 37
        // Up: 38
        // Right: 39
        // Down: 40
        if (this.keyCode == 37) {
            this.getPlayer().pressLeft();
        } else if (this.keyCode == 39) {
            this.getPlayer().pressRight();
        } else if (this.keyCode == 38) {
            this.getPlayer().pressUp();
        } else if (this.keyCode == 40) {
            this.getPlayer().pressDown();
        }
    }

    /**
     * Called every frame to detect if a key is released.
     * @see lawnlayer.Player#releaseUp()
     * @see lawnlayer.Player#releaseRight()
     * @see lawnlayer.Player#releaseDown()
     * @see lawnlayer.Player#releaseLeft()
     */
    public void keyReleased() {
        // Left: 37
        // Up: 38
        // Right: 39
        // Down: 40
        if (this.keyCode == 37) {
            this.getPlayer().releaseLeft();
        } else if (this.keyCode == 39) {
            this.getPlayer().releaseRight();
        } else if (this.keyCode == 38) {
            this.getPlayer().releaseUp();
        } else if (this.keyCode == 40) {
            this.getPlayer().releaseDown();
        }
    }

    /**
     * Plays the sound clip specified by the file path.
     * @param path the file path string 
     * @throws UnsupportedAudioFileException if audio file not supported
     * @throws IOException                   if fails to open a file
     * @throws LineUnavailableException      if audio file is unavailable or being 
     *                                       used by other applications
     */
    public void playSound(String path) throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        AudioInputStream audioInputStream = 
                AudioSystem.getAudioInputStream(new File(path).getAbsoluteFile());
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
    }

    public static void main(String[] args) {
        PApplet.main("lawnlayer.App");
    }
}
