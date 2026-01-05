import java.awt.Color;
import java.awt.Image;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class Escenario extends JFrame {

    public interface RunListener {
        void onRun(int[][] matrixSnapshot);
    }

    private RunListener runListener;

    private JLabel[][] tablero;
    private int[][] matrix;

    // Nuevo tamaño (Pac-Man clásico): 31 filas x 28 columnas
    private final int ROWS = 31;
    private final int COLS = 28;

    private static final int CELL = 30;
    private static final int OFFSET = 10;

    // Fondo
    private BackgroundPanel fondo;

    // Íconos
    private ImageIcon wallIcon;
    private ImageIcon pelletIcon;
    private ImageIcon pacmanIcon;
    private ImageIcon[] ghostIcons;

    private final JMenu settings = new JMenu("Settings");
    private final JMenu tools = new JMenu("Tools");

    private final JMenuItem runItem = new JMenuItem("Run");
    private final JMenuItem exitItem = new JMenuItem("Exit");
    private final JMenuItem reloadPresetItem = new JMenuItem("Reload Preset");
    private final JMenuItem toggleEditItem = new JMenuItem("Toggle Edit Mode");

    private final JRadioButtonMenuItem optWall = new JRadioButtonMenuItem("Wall");
    private final JRadioButtonMenuItem optPellet = new JRadioButtonMenuItem("Pellet");
    private final JRadioButtonMenuItem optEraser = new JRadioButtonMenuItem("Eraser");

    private ImageIcon actualIcon = null;
    private boolean editEnabled = false; //  por defecto: prearmado (no edición)

    private final JLabel hud = new JLabel("Score: 0 | Lives: 3");

    // (Opcional) coordenada spawn detectada del mapa ASCII
    private int pacmanSpawnR = -1, pacmanSpawnC = -1;

    public Escenario() {
        setTitle("PacMan Multiagentes (JADE)");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Ajuste de tamaño del frame a ROWS/COLS
        setBounds(50, 50, COLS * CELL + 35, ROWS * CELL + 120);

        // Fondo
        fondo = new BackgroundPanel("imagenes/fondo.jpg");
        setContentPane(fondo);
        fondo.setLayout(null);

        initIcons();
        initComponents();
        formaPlano();

        // Cargar escenario prearmado (ASCII)
        loadPresetScenario();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                int r = JOptionPane.showConfirmDialog(rootPane, "Desea salir?", "Aviso",
                        JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) System.exit(0);
            }
        });
    }

    public void setRunListener(RunListener listener) {
        this.runListener = listener;
    }

    private void initIcons() {
        wallIcon = scaledIcon("imagenes/Muro.jpg");
        pelletIcon = scaledIcon("imagenes/Energia.png");
        pacmanIcon = scaledIcon("imagenes/PacMan.png");
        ghostIcons = new ImageIcon[] {
                scaledIcon("imagenes/GhostY.png"),
                scaledIcon("imagenes/GhostB.png"),
                scaledIcon("imagenes/GhostR.png"),
                scaledIcon("imagenes/GhostG.png")
        };
    }

    private ImageIcon scaledIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(CELL, CELL, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void initComponents() {
        hud.setBounds(OFFSET, ROWS * CELL + 15, 900, 25);
        fondo.add(hud);

        ButtonGroup group = new ButtonGroup();
        group.add(optWall);
        group.add(optPellet);
        group.add(optEraser);

        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");

        setJMenuBar(bar);
        bar.add(file);
        bar.add(settings);
        bar.add(tools);

        file.add(runItem);
        file.add(exitItem);

        settings.add(optWall);
        settings.add(optPellet);
        settings.add(optEraser);

        tools.add(reloadPresetItem);
        tools.add(toggleEditItem);

        optWall.addItemListener(e -> { if (optWall.isSelected()) actualIcon = wallIcon; });
        optPellet.addItemListener(e -> { if (optPellet.isSelected()) actualIcon = pelletIcon; });
        optEraser.addItemListener(e -> { if (optEraser.isSelected()) actualIcon = null; });

        runItem.addActionListener(e -> gestionaRun());
        exitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        reloadPresetItem.addActionListener(e -> loadPresetScenario());
        toggleEditItem.addActionListener(e -> {
            editEnabled = !editEnabled;
            settings.setEnabled(editEnabled);
            hud.setText("Score: 0 | Lives: 3" + (editEnabled ? " | EDIT ON" : " | EDIT OFF"));
        });

        // Por defecto, edición deshabilitada (porque es prearmado)
        settings.setEnabled(editEnabled);
    }

    private void gestionaRun() {
        // bloquea todo al correr
        settings.setEnabled(false);
        tools.setEnabled(false);
        runItem.setEnabled(false);

        if (runListener != null) {
            runListener.onRun(getMatrixSnapshot());
        }
    }

    private void formaPlano() {
        tablero = new JLabel[ROWS][COLS];
        matrix = new int[ROWS][COLS];

        Border border = BorderFactory.createDashedBorder(Color.white);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                matrix[r][c] = 0;

                JLabel cell = new JLabel();
                cell.setBounds(c * CELL + OFFSET, r * CELL + OFFSET, CELL, CELL);
                cell.setBorder(border);
                cell.setOpaque(false);

                fondo.add(cell);

                final int rr = r, cc = c;
                cell.addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) { insertaObjeto(rr, cc); }
                    @Override public void mouseReleased(MouseEvent e) { insertaObjeto(rr, cc); }
                });

                tablero[r][c] = cell;
            }
        }
    }

    private void insertaObjeto(int r, int c) {
        if (!editEnabled) return;

        JLabel casilla = tablero[r][c];

        if (actualIcon == null) {
            casilla.setIcon(null);
            matrix[r][c] = 0;
            return;
        }

        casilla.setIcon(actualIcon);

        if (actualIcon == wallIcon) matrix[r][c] = 1;
        else if (actualIcon == pelletIcon) matrix[r][c] = 2;
        else matrix[r][c] = 0;
    }

    public int[][] getMatrixSnapshot() {
        int[][] copy = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(matrix[r], 0, copy[r], 0, COLS);
        }
        return copy;
    }

    // =========================
    // ESCENARIO PREARMADO (ASCII)
    // =========================
    private void loadPresetScenario() {
        // 0 = vacío, 1 = muro, 2 = pellet
        int[][] parsed = presetFromAscii();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                matrix[r][c] = parsed[r][c];
            }
        }

        repaintFromMatrix();
        hud.setText("Score: 0 | Lives: 3 | PRESET LOADED"
                + (pacmanSpawnR >= 0 ? (" | P@" + pacmanSpawnR + "," + pacmanSpawnC) : ""));
    }

    private void repaintFromMatrix() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int v = matrix[r][c];
                if (v == 1) tablero[r][c].setIcon(wallIcon);
                else if (v == 2) tablero[r][c].setIcon(pelletIcon);
                else tablero[r][c].setIcon(null);
            }
        }
    }

    /**
     *  MAPA ASCII (31x28)
     *
     * Convención:
     *   # = muro
     *   . = pellet
     *   v = vacío (pasillo sin pellet / túnel / etc.)
     *   g/G = casa de fantasmas (interior) -> vacío
     *   d/D = puerta (vacío)
     *   p/P = spawn pacman -> vacío (y lo guardamos en pacmanSpawnR/C)
     *
     *  SI QUIERES AJUSTAR MANUALMENTE LOS OBSTÁCULOS:
     *     EDITA DIRECTAMENTE ESTE BLOQUE "MAP" (aquí “formas” el laberinto)
     */
    private int[][] presetFromAscii() {

        final String[] MAP = new String[] {
                "############################",
                "#............##............#",
                "#.####.#####.##.####.#####.#",
                "#.####.#####.##.####.#####.#",
                "#.####.#####.##.####.#####.#",
                "#..........................#",
                "#.####.##.########.##.####.#",
                "#.####.##.########.##.####.#",
                "#......##....##....##......#",
                "######.#####.##.#####.######",
                "vvvvv#.#####.##.#####.#vvvvv",
                "vvvvv#.##..........##.#vvvvv",
                "vvvvv#.##.###dd###.##.#vvvvv",
                "######.##.#gggggg#.##.######",
                "vvvvvv....#gggggg#....vvvvvv",
                "######.##.#gggggg#.##.######",
                "vvvvv#.##.########.##.#vvvvv",
                "vvvvv#.##..........##.#vvvvv",
                "vvvvv#.##.########.##.#vvvvv",
                "######.##.########.##.######",
                "#............##............#",
                "#.####.#####.##.#####.####.#",
                "#.####.#####.##.#####.####.#",
                "#...##.......pp.......##...#",
                "###.##.##.########.##.##.###",
                "###.##.##.########.##.##.###",
                "#......##....##....##......#",
                "#.##########.##.##########.#",
                "#.##########.##.##########.#",
                "#..........................#",
                "############################"
        };

        // Validación rápida (por si cambias algo y se descuadra)
        if (MAP.length != ROWS) {
            throw new IllegalStateException("MAP rows=" + MAP.length + " pero ROWS=" + ROWS);
        }
        for (int i = 0; i < MAP.length; i++) {
            if (MAP[i].length() != COLS) {
                throw new IllegalStateException("MAP row " + i + " len=" + MAP[i].length() + " pero COLS=" + COLS);
            }
        }

        pacmanSpawnR = -1; pacmanSpawnC = -1;

        int[][] out = new int[ROWS][COLS];

        for (int r = 0; r < ROWS; r++) {
            String line = MAP[r];
            for (int c = 0; c < COLS; c++) {
                char ch = line.charAt(c);

                switch (ch) {
                    case '#': out[r][c] = 1; break;        // muro
                    case '.': out[r][c] = 2; break;        // pellet
                    case 'v': out[r][c] = 0; break;        // vacío (sin pellet)
                    case 'g': case 'G': out[r][c] = 0; break; // interior casa fantasmas (vacío)
                    case 'd': case 'D': out[r][c] = 0; break; // puerta (vacío)
                    case 'p': case 'P':
                        out[r][c] = 0;                     // spawn pacman (vacío)
                        // guardamos el primer 'p' como referencia
                        if (pacmanSpawnR < 0) { pacmanSpawnR = r; pacmanSpawnC = c; }
                        break;
                    default:
                        // Cualquier otro char lo tomamos como vacío para no romper
                        out[r][c] = 0;
                        break;
                }
            }
        }

        return out;
    }

    // Render usado por UIAgent (cuando World manda estado)
    public void render(GameState s) {
        SwingUtilities.invokeLater(() -> {
            // Más robusto si tu GameState aún trae dim viejo: tomamos el tamaño real del map
            int rMax = Math.min(ROWS, s.map != null ? s.map.length : 0);
            int cMax = (rMax > 0) ? Math.min(COLS, s.map[0].length) : 0;

            for (int r = 0; r < rMax; r++) {
                for (int c = 0; c < cMax; c++) {
                    JLabel cell = tablero[r][c];
                    int v = s.map[r][c];
                    if (v == 1) cell.setIcon(wallIcon);
                    else if (v == 2) cell.setIcon(pelletIcon);
                    else cell.setIcon(null);
                }
            }

            // Pacman
            if (s.pacmanR >= 0 && s.pacmanC >= 0 && s.pacmanR < ROWS && s.pacmanC < COLS) {
                tablero[s.pacmanR][s.pacmanC].setIcon(pacmanIcon);
            }

            // Fantasmas
            int idx = 0;
            for (var entry : s.ghosts.entrySet()) {
                int[] pos = entry.getValue();
                if (pos == null) continue;
                if (pos[0] < 0 || pos[1] < 0 || pos[0] >= ROWS || pos[1] >= COLS) continue;

                ImageIcon gi = ghostIcons[idx % ghostIcons.length];
                tablero[pos[0]][pos[1]].setIcon(gi);
                idx++;
            }

            hud.setText("Score: " + s.score + " | Lives: " + s.lives + (s.running ? "" : " | (waiting map)"));
        });
    }
}
