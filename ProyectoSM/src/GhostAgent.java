import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class GhostAgent extends Agent {

    private AID worldAid = new AID("world", AID.ISLOCALNAME);

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " listo (GhostAgent).");

        ACLMessage sub = new ACLMessage(ACLMessage.SUBSCRIBE);
        sub.addReceiver(worldAid);
        sub.setContent("ghost");
        send(sub);

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) { block(); return; }

                if (msg.getPerformative() == ACLMessage.INFORM &&
                        "STATE_UPDATE".equals(msg.getConversationId())) {
                    try {
                        GameState s = (GameState) msg.getContentObject();
                        if (!s.running) return;

                        int[] myPos = s.ghosts.get(getLocalName());
                        if (myPos == null) return;

                        GameState.GhostMode mode = s.ghostModes.getOrDefault(getLocalName(), GameState.GhostMode.ACTIVE);

                        Direction d;
                        if (mode == GameState.GhostMode.IN_HOUSE) {
                            d = Direction.NONE;
                        } else if (mode == GameState.GhostMode.LEAVING) {
                            d = bfsFirstStep(s, myPos[0], myPos[1], s.doorR, s.doorC);
                        } else {
                            // ACTIVE: perseguir Pac-Man con BFS
                            d = bfsFirstStep(s, myPos[0], myPos[1], s.pacmanR, s.pacmanC);
                        }

                        sendMove(d);

                    } catch (Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        });
    }

    private void sendMove(Direction d) {
        ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
        req.addReceiver(worldAid);
        req.setConversationId("MOVE_REQUEST");
        req.setContent(d.name());
        send(req);
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

    /**
     * BFS: retorna el primer paso desde (sr,sc) a (tr,tc).
     * Ghosts pueden pasar por casa/puerta (todo salvo muros).
     */
    private Direction bfsFirstStep(GameState s, int sr, int sc, int tr, int tc) {
        if (sr == tr && sc == tc) return Direction.NONE;

        int R = rows(s), C = cols(s);

        boolean[][] vis = new boolean[R][C];
        int[][] prevDir = new int[R][C]; // Direction ordinal+1, 0=none
        int[][] pr = new int[R][C];
        int[][] pc = new int[R][C];

        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{sr, sc});
        vis[sr][sc] = true;

        for (int r = 0; r < R; r++) Arrays.fill(prevDir[r], 0);

        Direction[] dirs = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];

            for (Direction d : dirs) {
                int[] nx = stepWithTunnel(s, r, c, d);
                int nr = nx[0], nc = nx[1];

                if (nr < 0 || nr >= R) continue; // solo wrap horizontal
                if (vis[nr][nc]) continue;
                if (s.map[nr][nc] == 1) continue; // muro

                vis[nr][nc] = true;
                pr[nr][nc] = r;
                pc[nr][nc] = c;
                prevDir[nr][nc] = d.ordinal() + 1;

                if (nr == tr && nc == tc) {
                    return reconstructFirst(sr, sc, tr, tc, pr, pc, prevDir);
                }

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
}
