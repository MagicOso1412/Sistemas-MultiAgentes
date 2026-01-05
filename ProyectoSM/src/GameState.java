import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameState implements Serializable {
    public int dim;

    /**
     * 0 vacío, 1 muro, 2 pellet
     * (World normaliza 3/4/5 a 0 al recibir MAP_INIT)
     */
    public int[][] map;

    // Spawns
    public int pacmanR, pacmanC;
    public int pacmanSpawnR = -1, pacmanSpawnC = -1;

    // Casa de fantasmas
    public boolean[][] ghostHouse; // true = celda interior casa (G)
    public int doorR = -1, doorC = -1; // puerta (D)

    // ghosts: name -> [r,c]
    public Map<String, int[]> ghosts = new HashMap<>();

    // modo de cada fantasma
    public enum GhostMode { IN_HOUSE, LEAVING, ACTIVE }
    public Map<String, GhostMode> ghostModes = new HashMap<>();

    public int score = 0;
    public int lives = 3;
    public boolean running = false;

    // (opcional) tick para debug/IA
    public int tick = 0;
}
