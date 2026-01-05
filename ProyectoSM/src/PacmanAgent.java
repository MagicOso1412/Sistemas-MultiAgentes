import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class PacmanAgent extends Agent {

    private final Random rnd = new Random();
    private AID worldAid = new AID("world", AID.ISLOCALNAME);
    private GameState last;

    private final int DANGER_DIST = 3;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " listo (PacmanAgent).");

        ACLMessage sub = new ACLMessage(ACLMessage.SUBSCRIBE);
        sub.addReceiver(worldAid);
        sub.setContent("pacman");
        send(sub);

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) { block(); return; }

                if (msg.getPerformative() == ACLMessage.INFORM &&
                        "STATE_UPDATE".equals(msg.getConversationId())) {
                    try {
                        last = (GameState) msg.getContentObject();
                        if (!last.running) return;

                        Direction d = decide(last);
                        sendMove(d);

                    } catch (Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        });
    }

    private int rows(GameState s) { return s.map.length; }
    private int cols(GameState s) { return s.map[0].length; }

    private int[] stepWithTunnel(GameState s, int r, int c, Direction d) {
        int nr = r, nc = c;
        switch (d) {
            case UP -> nr--;
            case DOWN -> nr++;
            case LEFT -> nc--;
            case RIGHT -> nc++;
            default -> { }
        }

        // wrap horizontal
        if (d == Direction.LEFT && nc < 0) nc = cols(s) - 1;
        if (d == Direction.RIGHT && nc >= cols(s)) nc = 0;

        return new int[]{nr, nc};
    }

    private Direction decide(GameState s) {
        // 1) si hay fantasma cerca, huir
        int nearest = Integer.MAX_VALUE;
        int ngR = -1, ngC = -1;
        for (int[] g : s.ghosts.values()) {
            if (g == null) continue;
            int dist = Math.abs(g[0] - s.pacmanR) + Math.abs(g[1] - s.pacmanC);
            if (dist < nearest) {
                nearest = dist;
                ngR = g[0]; ngC = g[1];
            }
        }

        if (nearest <= DANGER_DIST && ngR >= 0) {
            return flee(s, ngR, ngC);
        }

        // 2) buscar pellet más cercano con BFS
        Direction towardPellet = bfsToNearestPellet(s);
        if (towardPellet != Direction.NONE) return towardPellet;

        // 3) fallback: random legal
        return pickRandomLegalMove(s, s.pacmanR, s.pacmanC);
    }

    private Direction flee(GameState s, int gr, int gc) {
        List<Direction> legal = legalMovesPacman(s, s.pacmanR, s.pacmanC);
        if (legal.isEmpty()) return Direction.NONE;

        int best = Integer.MIN_VALUE;
        List<Direction> bestDirs = new ArrayList<>();

        for (Direction d : legal) {
            int[] nx = stepWithTunnel(s, s.pacmanR, s.pacmanC, d);
            int nr = nx[0], nc = nx[1];

            int dist = Math.abs(nr - gr) + Math.abs(nc - gc);
            if (dist > best) {
                best = dist;
                bestDirs.clear();
                bestDirs.add(d);
            } else if (dist == best) {
                bestDirs.add(d);
            }
        }
        return bestDirs.get(rnd.nextInt(bestDirs.size()));
    }

    private Direction bfsToNearestPellet(GameState s) {
        int R = rows(s), C = cols(s);
        int sr = s.pacmanR, sc = s.pacmanC;

        boolean[][] vis = new boolean[R][C];
        int[][] pr = new int[R][C];
        int[][] pc = new int[R][C];
        int[][] prevDir = new int[R][C]; // Direction ordinal+1, 0=none

        for (int r = 0; r < R; r++) Arrays.fill(prevDir[r], 0);

        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{sr, sc});
        vis[sr][sc] = true;

        Direction[] dirs = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];

            // pellet (no contar celda inicial)
            if (!(r == sr && c == sc) && s.map[r][c] == 2) {
                return reconstructFirst(sr, sc, r, c, pr, pc, prevDir);
            }

            for (Direction d : dirs) {
                int[] nx = stepWithTunnel(s, r, c, d);
                int nr = nx[0], nc = nx[1];

                if (nr < 0 || nr >= R) continue; // solo wrap horizontal
                if (vis[nr][nc]) continue;
                if (s.map[nr][nc] == 1) continue; // muro

                // Pac-Man no entra a casa ni a puerta
                if (s.ghostHouse != null && s.ghostHouse[nr][nc]) continue;
                if (nr == s.doorR && nc == s.doorC) continue;

                vis[nr][nc] = true;
                pr[nr][nc] = r;
                pc[nr][nc] = c;
                prevDir[nr][nc] = d.ordinal() + 1;
                q.add(new int[]{nr, nc});
            }
        }

        return Direction.NONE;
    }

    private Direction reconstructFirst(int sr, int sc, int tr, int tc,
                                       int[][] pr, int[][] pc, int[][] prevDir) {
        int r = tr, c = tc;
        while (!(pr[r][c] == sr && pc[r][c] == sc)) {
            int rr = pr[r][c];
            int cc = pc[r][c];
            if (rr == r && cc == c) break;
            r = rr; c = cc;
        }
        int code = prevDir[r][c];
        if (code == 0) return Direction.NONE;
        return Direction.values()[code - 1];
    }

    private void sendMove(Direction d) {
        ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
        req.addReceiver(worldAid);
        req.setConversationId("MOVE_REQUEST");
        req.setContent(d.name());
        send(req);
    }

    private Direction pickRandomLegalMove(GameState s, int r, int c) {
        List<Direction> legal = legalMovesPacman(s, r, c);
        if (legal.isEmpty()) return Direction.NONE;
        return legal.get(rnd.nextInt(legal.size()));
    }

    private List<Direction> legalMovesPacman(GameState s, int r, int c) {
        int R = rows(s), C = cols(s);
        List<Direction> legal = new ArrayList<>();

        for (Direction d : new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT}) {
            int[] nx = stepWithTunnel(s, r, c, d);
            int nr = nx[0], nc = nx[1];

            if (nr < 0 || nr >= R) continue;
            if (nc < 0 || nc >= C) continue;

            if (s.map[nr][nc] == 1) continue;
            if (s.ghostHouse != null && s.ghostHouse[nr][nc]) continue;
            if (nr == s.doorR && nc == s.doorC) continue;

            legal.add(d);
        }
        return legal;
    }
}
