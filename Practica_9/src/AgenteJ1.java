import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import java.util.Random;

public class AgenteJ1 extends Agent {
    private Random random = new Random();

    @Override
    protected void setup() {
        System.out.println("Jugador 1 iniciado: " + getLocalName());

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String contenido = msg.getContent();

                    if (contenido.equals("TU_TURNO")) {
                        int fila = random.nextInt(3);
                        int col = random.nextInt(3);
                        ACLMessage jugada = new ACLMessage(ACLMessage.INFORM);
                        jugada.addReceiver(new AID("Arbitro", AID.ISLOCALNAME));
                        jugada.setContent(fila + "," + col);
                        send(jugada);
                        System.out.println("J1 juega [" + fila + "," + col + "]");
                    } else if (contenido.equals("JUGADA_INVALIDA")) {
                        System.out.println("J1 hizo jugada inválida, intentando de nuevo...");
                        int fila = random.nextInt(3);
                        int col = random.nextInt(3);
                        ACLMessage jugada = new ACLMessage(ACLMessage.INFORM);
                        jugada.addReceiver(new AID("Arbitro", AID.ISLOCALNAME));
                        jugada.setContent(fila + "," + col);
                        send(jugada);
                    } else if (contenido.equals("FIN_EMPATE")) {
                        System.out.println("J1: Empate. Fin del juego.");
                        doDelete();
                    } else if (contenido.startsWith("FIN_GANA")) {
                        System.out.println("J1: " + contenido);
                        doDelete();
                    }
                } else {
                    block();
                }
            }
        });
    }
}
