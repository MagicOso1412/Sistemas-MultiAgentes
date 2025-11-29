
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class AgenteArbitro extends Agent {
    private Tablero tablero = new Tablero();
    private char turnoActual = 'X'; // J1 empieza

    @Override
    protected void setup() {
        System.out.println("Árbitro iniciado: " + getLocalName());

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String contenido = msg.getContent();
                    String jugador = msg.getSender().getLocalName();
                    char simbolo = jugador.equals("J1") ? 'X' : 'O';

                    String[] partes = contenido.split(",");
                    int fila = Integer.parseInt(partes[0]);
                    int col = Integer.parseInt(partes[1]);

                    if (tablero.marcar(fila, col, simbolo)) {
                        System.out.println("" + jugador + " juega en [" + fila + "," + col + "]");
                        System.out.println(tablero.mostrar());

                        if (tablero.hayGanador(simbolo)) {
                            System.out.println("" + jugador + " ha ganado!");
                            notificar("FIN_GANA:" + jugador);
                            doDelete();
                        } else if (tablero.estaLleno()) {
                            System.out.println("Empate!");
                            notificar("FIN_EMPATE");
                            doDelete();
                        } else {
                            turnoActual = (turnoActual == 'X') ? 'O' : 'X';
                            String siguiente = (turnoActual == 'X') ? "J1" : "J2";
                            enviar(siguiente, "TU_TURNO");
                        }
                    } else {
                        enviar(jugador, "JUGADA_INVALIDA");
                    }
                } else {
                    block();
                }
            }
        });

        // Iniciar el primer turno
        enviar("J1", "TU_TURNO");
    }

    private void enviar(String receptor, String contenido) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receptor, AID.ISLOCALNAME));
        msg.setContent(contenido);
        send(msg);
    }

    private void notificar(String contenido) {
        enviar("J1", contenido);
        enviar("J2", contenido);
    }
}
