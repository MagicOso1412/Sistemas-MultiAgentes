import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class WorldAgent extends Agent {

    private final int TICK_MS = 200;

    // salida de fantasmas (1 por 1)
    private final int RELEASE_EVERY_TICKS = 20; // 20 ticks * 200ms = 4s aprox
    private int releaseCooldown = 0;

    private GameState state = new GameState();

    private AID uiAid;
    private AID pacmanAid;
    private final Map<String, AID> ghostAids = new LinkedHashMap<>();

    // movimientos pendientes
    private final Map<String, Direction> pendingMoves = new HashMap<>();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " listo (WorldAgent).");

        // Recibe: SUBSCRIBE, MAP_INIT, MOVE_REQUEST
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) { block(); return; }

                String conv = msg.getConversationId();

                try {
                    switch (msg.getPerformative()) {
                        case ACLMessage.SUBSCRIBE -> handleSubscribe(msg);
                        case ACLMessage.INFORM -> {
                            if ("MAP_INIT".equals(conv)) handleMapInit(msg);
                        }
                        case ACLMessage.REQUEST -> {
                            if ("MOVE_REQUEST".equals(conv)) handleMoveRequest(msg);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
        });

        // Loop del juego
        addBehaviour(new TickerBehaviour(this, TICK_MS) {
            @Override
            protected void onTick() {
                if (!state.running) return;

                state.tick++;

                // 0) liberación gradual (controlada por world)
                handleGhostReleaseSchedule();

                // 1) aplicar movimiento pacman
                applyMoveIfPresent("pacman", pacmanAid);

                // 2) aplicar movimientos fantasmas
                for (String gName : ghostAids.keySet()) {
                    applyMoveIfPresent(gName, ghostAids.get(gName));
                }

                // 3) pellet
                if (state.map != null && state.map[state.pacmanR][state.pacmanC] == 2) {
                    state.map[state.pacmanR][state.pacmanC] = 0;
                    state.score += 10;
                }

                // 4) colisión pacman vs fantasmas
                for (Map.Entry<String, int[]> e : state.ghosts.entrySet()) {
                    int[] gpos = e.getValue();
                    if (gpos == null) continue;
                    if (gpos[0] == state.pacmanR && gpos[1] == state.pacmanC) {
                        state.lives--;
                        resetPositions();
                        break;
                    }
                }

                // 5) fin
                if (state.lives <= 0) {
                    state.running = false;
                }

                // 6) broadcast estado
                broadcastState();
            }
        });
    }

    private void handleSubscribe(ACLMessage msg) {
        String role = msg.getContent(); // "ui" | "pacman" | "ghost"
        String name = msg.getSender().getLocalName();

        if ("ui".equalsIgnoreCase(role)) {
            uiAid = msg.getSender();
            System.out.println("UI suscrita: " + name);
            if (state.map != null) sendStateTo(uiAid);
            return;
        }

        if ("pacman".equalsIgnoreCase(role)) {
            pacmanAid = msg.getSender();
            System.out.println("Pacman suscrito: " + name);

            // Si el mapa ya estaba inicializado, asegúrate de posicionar a Pac-Man en su spawn
            if (state.map != null) {
                if (state.pacmanSpawnR >= 0) {
                    state.pacmanR = state.pacmanSpawnR;
                    state.pacmanC = state.pacmanSpawnC;
                }
                sendStateTo(pacmanAid);
                if (uiAid != null) sendStateTo(uiAid);
            }
            return;
        }

        if ("ghost".equalsIgnoreCase(role)) {
            ghostAids.put(name, msg.getSender());
            System.out.println("Ghost suscrito: " + name);

            // Si el mapa ya está inicializado, spawnea este ghost aunque MAP_INIT haya llegado antes
            if (state.map != null) {
                ensureGhostSpawned(name);
                sendStateTo(msg.getSender());
                if (uiAid != null) sendStateTo(uiAid);
                if (pacmanAid != null) sendStateTo(pacmanAid);
            }
        }
    }

    /**
     * MAP_INIT recibe un int[][].
     * Además de 0/1/2, acepta:
     * 3=G (casa fantasmas), 4=D (puerta), 5=P (spawn pacman)
     */
    private void handleMapInit(ACLMessage msg) throws Exception {
        int[][] raw = (int[][]) msg.getContentObject();

        state = new GameState();
        state.map = deepCopy(raw);

        // dim se queda como filas (compatibilidad), PERO ya no usamos dim para columnas
        state.dim = rows();

        state.ghostHouse = new boolean[rows()][cols()];

        state.running = true;
        state.score = 0;
        state.lives = 3;
        state.tick = 0;

        // 1) detectar y normalizar especiales (3/4/5 => 0 en map)
        List<int[]> houseCells = new ArrayList<>();
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                int v = state.map[r][c];
                if (v == 3) { // G
                    state.ghostHouse[r][c] = true;
                    houseCells.add(new int[]{r, c});
                    state.map[r][c] = 0;
                } else if (v == 4) { // D
                    state.doorR = r;
                    state.doorC = c;
                    state.map[r][c] = 0;
                } else if (v == 5) { // P
                    state.pacmanSpawnR = r;
                    state.pacmanSpawnC = c;
                    state.map[r][c] = 0;
                }
            }
        }

        // 2) spawns fallback si no venían marcados
        if (state.pacmanSpawnR < 0) {
            state.pacmanSpawnR = rows() - 2;
            state.pacmanSpawnC = 1;
            if (!isInside(state.pacmanSpawnR, state.pacmanSpawnC) || isWall(state.pacmanSpawnR, state.pacmanSpawnC)) {
                int[] p = findFirstFreeForPacman();
                state.pacmanSpawnR = p[0];
                state.pacmanSpawnC = p[1];
            }
        }

        // puerta fallback si no existe: celda libre cerca del centro
        if (state.doorR < 0) {
            int midR = rows() / 2;
            int midC = cols() / 2;
            int[] d = findNearestFree(midR, midC);
            state.doorR = d[0];
            state.doorC = d[1];
        }

        // casa fallback si no existe: celdas cerca del centro (no muros)
        if (houseCells.isEmpty()) {
            int midR = rows() / 2;
            int midC = cols() / 2;
            for (int rr = midR - 1; rr <= midR + 1; rr++) {
                for (int cc = midC - 2; cc <= midC + 2; cc++) {
                    if (isInside(rr, cc) && !isWall(rr, cc)) {
                        state.ghostHouse[rr][cc] = true;
                        houseCells.add(new int[]{rr, cc});
                    }
                }
            }
        }

        // 3) colocar pacman en su spawn
        state.pacmanR = state.pacmanSpawnR;
        state.pacmanC = state.pacmanSpawnC;

        // 4) colocar fantasmas dentro de la casa, con modo IN_HOUSE
        state.ghosts.clear();
        state.ghostModes.clear();

        int idx = 0;
        for (String gName : ghostAids.keySet()) {
            int[] cell = houseCells.isEmpty() ? findFirstFree() : houseCells.get(idx % houseCells.size());
            state.ghosts.put(gName, new int[]{cell[0], cell[1]});
            state.ghostModes.put(gName, GameState.GhostMode.IN_HOUSE);
            idx++;
        }

        pendingMoves.clear();
        releaseCooldown = RELEASE_EVERY_TICKS;

        System.out.println("Mapa inicializado. rows=" + rows() + " cols=" + cols()
                + " | P=(" + state.pacmanSpawnR + "," + state.pacmanSpawnC + ")"
                + " | D=(" + state.doorR + "," + state.doorC + ")"
                + " | ghosts=" + state.ghosts.keySet()
        );

        broadcastState();
    }

    private void handleMoveRequest(ACLMessage msg) {
        String agentName = msg.getSender().getLocalName();
        Direction dir = Direction.valueOf(msg.getContent());
        pendingMoves.put(agentName, dir);
    }

    /**
     * Libera 1 fantasma cada RELEASE_EVERY_TICKS, sólo si no hay uno ya "LEAVING".
     */
    private void handleGhostReleaseSchedule() {
        if (releaseCooldown > 0) releaseCooldown--;

        // si ya hay uno saliendo, no liberamos otro
        for (GameState.GhostMode m : state.ghostModes.values()) {
            if (m == GameState.GhostMode.LEAVING) return;
        }

        if (releaseCooldown > 0) return;

        // busca el siguiente IN_HOUSE, por orden
        for (String gName : ghostAids.keySet()) {
            GameState.GhostMode m = state.ghostModes.get(gName);
            if (m == GameState.GhostMode.IN_HOUSE) {
                state.ghostModes.put(gName, GameState.GhostMode.LEAVING);
                releaseCooldown = RELEASE_EVERY_TICKS;
                break;
            }
        }
    }

    private void applyMoveIfPresent(String logicalName, AID aid) {
        if (aid == null || state.map == null) return;

        String keyName = aid.getLocalName();
        Direction dir = pendingMoves.getOrDefault(keyName, Direction.NONE);
        if (dir == Direction.NONE) return;

        if ("pacman".equals(logicalName)) {
            int[] next = nextCellWithTunnel(state.pacmanR, state.pacmanC, dir);

            // Pac-Man NO entra a la casa ni cruza la puerta
            if (isInside(next[0], next[1])
                    && !isWall(next[0], next[1])
                    && !isGhostHouse(next[0], next[1])
                    && !isDoor(next[0], next[1])) {
                state.pacmanR = next[0];
                state.pacmanC = next[1];
            }

        } else {
            int[] gpos = state.ghosts.get(keyName);

            // si por timing el ghost envió MOVE antes de tener posición, créala al vuelo
            if (gpos == null) {
                ensureGhostSpawned(keyName);
                gpos = state.ghosts.get(keyName);
                if (gpos == null) return;
            }

            GameState.GhostMode mode = state.ghostModes.getOrDefault(keyName, GameState.GhostMode.ACTIVE);

            // si está IN_HOUSE, ignora sus moves (se queda quieto)
            if (mode == GameState.GhostMode.IN_HOUSE) return;

            int[] next = nextCellWithTunnel(gpos[0], gpos[1], dir);

            if (isInside(next[0], next[1]) && !isWall(next[0], next[1])) {
                gpos[0] = next[0];
                gpos[1] = next[1];
            }

            // transición LEAVING -> ACTIVE cuando llega a la puerta
            if (mode == GameState.GhostMode.LEAVING) {
                if (gpos[0] == state.doorR && gpos[1] == state.doorC) {
                    state.ghostModes.put(keyName, GameState.GhostMode.ACTIVE);
                }
            }
        }
    }

    private void resetPositions() {
        if (state.map == null) return;

        // pacman al spawn real
        state.pacmanR = state.pacmanSpawnR;
        state.pacmanC = state.pacmanSpawnC;

        // ghosts a la casa y reiniciar modos/schedule
        List<int[]> houseCells = new ArrayList<>();
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                if (state.ghostHouse != null && state.ghostHouse[r][c]) {
                    houseCells.add(new int[]{r, c});
                }
            }
        }

        int idx = 0;
        for (String gName : state.ghosts.keySet()) {
            int[] cell = houseCells.isEmpty() ? findFirstFree() : houseCells.get(idx % houseCells.size());
            state.ghosts.put(gName, new int[]{cell[0], cell[1]});
            state.ghostModes.put(gName, GameState.GhostMode.IN_HOUSE);
            idx++;
        }

        releaseCooldown = RELEASE_EVERY_TICKS;
        pendingMoves.clear();
    }

    private void broadcastState() {
        if (uiAid != null) sendStateTo(uiAid);
        if (pacmanAid != null) sendStateTo(pacmanAid);
        for (AID g : ghostAids.values()) sendStateTo(g);
    }

    private void sendStateTo(AID to) {
        try {
            ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
            inf.addReceiver(to);
            inf.setConversationId("STATE_UPDATE");
            inf.setContentObject(state);
            send(inf);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }

    // -------------------------
    // Spawn tardío ghosts
    // -------------------------
    private void ensureGhostSpawned(String gName) {
        if (state.map == null) return;
        if (state.ghosts.containsKey(gName)) return;

        int[] cell = pickHouseCellForIndex(state.ghosts.size());
        state.ghosts.put(gName, new int[]{cell[0], cell[1]});
        state.ghostModes.put(gName, GameState.GhostMode.IN_HOUSE);
    }

    private int[] pickHouseCellForIndex(int idx) {
        List<int[]> houseCells = new ArrayList<>();
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                if (state.ghostHouse != null && state.ghostHouse[r][c]) {
                    houseCells.add(new int[]{r, c});
                }
            }
        }
        if (houseCells.isEmpty()) return findFirstFree();
        return houseCells.get(idx % houseCells.size());
    }

    // -------------------------
    // TÚNELES laterales (wrap)
    // -------------------------
    private int[] nextCellWithTunnel(int r, int c, Direction d) {
        int nr = r, nc = c;

        switch (d) {
            case UP -> nr--;
            case DOWN -> nr++;
            case LEFT -> nc--;
            case RIGHT -> nc++;
            default -> { }
        }

        // wrap horizontal
        if (d == Direction.LEFT && nc < 0) nc = cols() - 1;
        if (d == Direction.RIGHT && nc >= cols()) nc = 0;

        return new int[]{nr, nc};
    }

    // -------------------------
    // Utils (rectangular)
    // -------------------------
    private int rows() { return (state.map == null) ? 0 : state.map.length; }
    private int cols() { return (state.map == null || state.map.length == 0) ? 0 : state.map[0].length; }

    private boolean isWall(int r, int c) { return state.map[r][c] == 1; }
    private boolean isInside(int r, int c) { return r >= 0 && c >= 0 && r < rows() && c < cols(); }

    private boolean isGhostHouse(int r, int c) {
        return state.ghostHouse != null && state.ghostHouse[r][c];
    }

    private boolean isDoor(int r, int c) {
        return r == state.doorR && c == state.doorC;
    }

    private int[][] deepCopy(int[][] m) {
        int[][] copy = new int[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) System.arraycopy(m[i], 0, copy[i], 0, m[i].length);
        return copy;
    }

    private int[] findFirstFreeForPacman() {
        for (int r = 0; r < rows(); r++)
            for (int c = 0; c < cols(); c++)
                if (state.map[r][c] != 1 && !isGhostHouse(r, c) && !isDoor(r, c))
                    return new int[]{r, c};
        return new int[]{0, 0};
    }

    private int[] findFirstFree() {
        for (int r = 0; r < rows(); r++)
            for (int c = 0; c < cols(); c++)
                if (state.map[r][c] != 1) return new int[]{r, c};
        return new int[]{0, 0};
    }

    private int[] findNearestFree(int sr, int sc) {
        if (!isInside(sr, sc)) { sr = rows() / 2; sc = cols() / 2; }

        boolean[][] vis = new boolean[rows()][cols()];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{sr, sc});
        vis[sr][sc] = true;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];
            if (!isWall(r, c)) return new int[]{r, c};

            for (Direction d : new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT}) {
                int[] nx = nextCellWithTunnel(r, c, d);
                int nr = nx[0], nc = nx[1];
                if (isInside(nr, nc) && !vis[nr][nc]) {
                    vis[nr][nc] = true;
                    q.add(new int[]{nr, nc});
                }
            }
        }
        return new int[]{0, 0};
    }
}
