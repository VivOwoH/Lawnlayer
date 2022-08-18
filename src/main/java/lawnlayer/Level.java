package lawnlayer;

import java.util.*;

/**
 * Represents a level of the game.
 */
public class Level {
    private String outlay;
    private String powerupType;
    private double goal;
    private List<HashMap<String,Object>> enemies;

    /**
     * Creates a new level with specified data parsed from config.
     * @param outlay    the layout .txt filepath that marks positions of concretes
     * @param powerupType   the type of powerup to spawn in this level
     * @param goal      the goal that needs to be reached to win this level
     */
    public Level(String outlay, String powerupType, double goal) {
        this.outlay = outlay;
        this.powerupType = powerupType;
        this.goal = goal;
        this.enemies = new ArrayList<HashMap<String,Object>>();
    }

    /**
     * Gets the layout filepath.
     * @return the layout .txt filepath
     */
    public String getOutlay() {
        return this.outlay;
    }

    /**
     * Gets the type of powerup to spawn in this level.
     * @return the powerup type string
     */
    public String getPowerupType() {
        return this.powerupType;
    }

    /**
     * Gets the level goal.
     * @return the level goal in fraction 
     */
    public double getGoal() {
        return this.goal * 100.0;
    }

    /**
     * Gets the list of enemies for this level.
     * @return the list of enemy datas
     */
    public List<HashMap<String,Object>> getEnemyList() {
        return this.enemies;
    }

    /**
     * Adds the enemy to the list of enemies for this level.
     * @param enemy the parsed enemy config data as a Map.
     *              Map key include enemy type, spawn information.
     */
    public void addEnemy(HashMap<String,Object> enemy) {
        this.enemies.add(enemy);
    }
    
    // // debug | level data
    // public String toString() {
    //     return String.format("outlay:%s%n goal:%f%n number of enemies:%d"
    //             ,this.outlay,this.goal,this.enemies.size());
    // }
}