

public class Tablero {
    private char[][] tablero = new char[3][3];

    public Tablero() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                tablero[i][j] = '-';
    }

    public boolean marcar(int fila, int columna, char jugador) {
        if (tablero[fila][columna] == '-') {
            tablero[fila][columna] = jugador;
            return true;
        }
        return false;
    }

    public boolean hayGanador(char jugador) {
        for (int i = 0; i < 3; i++) {
            if (tablero[i][0] == jugador && tablero[i][1] == jugador && tablero[i][2] == jugador)
                return true;
            if (tablero[0][i] == jugador && tablero[1][i] == jugador && tablero[2][i] == jugador)
                return true;
        }
        return (tablero[0][0] == jugador && tablero[1][1] == jugador && tablero[2][2] == jugador)
                || (tablero[0][2] == jugador && tablero[1][1] == jugador && tablero[2][0] == jugador);
    }

    public boolean estaLleno() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (tablero[i][j] == '-')
                    return false;
        return true;
    }

    public String mostrar() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++)
                sb.append(tablero[i][j]).append(' ');
            sb.append('\n');
        }
        return sb.toString();
    }
}
