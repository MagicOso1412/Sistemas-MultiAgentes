
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

public class AgenteArbitro extends Agent {

    private int ronda = 1;

    private final Map<String, Integer> ofertasActuales = new HashMap<>();
    private final Map<String, Integer> ofertasFinales = new HashMap<>();
    private final Map<String, Integer> argumentos = new HashMap<>();

    @Override
    protected void setup() {
        System.out.println("Agente Arbitro inicializado: " + getLocalName());

        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();

                if (msg != null) {
                    String contenido = msg.getContent();
                    String[] partes = contenido.split(":");

                    switch (partes[0]) {

                        case "OFERTA":
                            manejarOferta(msg.getSender().getLocalName(), Integer.parseInt(partes[1]));
                            break;

                        case "ARG":
                            manejarArgumento(msg.getSender().getLocalName(), Integer.parseInt(partes[1]));
                            break;
                    }

                } else {
                    block();
                }
            }
        });
    }

    private void manejarOferta(String competidor, int valor) {
        ofertasActuales.put(competidor, valor);
        System.out.println("Árbitro recibe oferta de " + competidor + ": " + valor);

        // SOLO ESPERA 2 OFERTAS
        if (ofertasActuales.size() == 2) {
            System.out.println("=== Ronda " + ronda + " completa ===");

            reenviarOfertas();

            if (ronda == 3) {
                ofertasFinales.putAll(ofertasActuales);
                System.out.println("\n=== Fin Fase 1: Ofertas finales ===");
                ofertasFinales.forEach((k, v) -> System.out.println(k + ": " + v));
                System.out.println("\n=== Fase 2: Esperando argumentos ===");
            } else {
                ronda++;
            }

            ofertasActuales.clear();
        }
    }

    private void reenviarOfertas() {
        int oferta1 = ofertasActuales.get("competidor1");
        int oferta2 = ofertasActuales.get("competidor2");

        enviarMensaje("competidor1", "OFERTAS:" + oferta1 + ":" + oferta2);
        enviarMensaje("competidor2", "OFERTAS:" + oferta1 + ":" + oferta2);
    }

    private void enviarMensaje(String receptor, String contenido) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receptor, AID.ISLOCALNAME));
        msg.setContent(contenido);
        send(msg);
    }

    private void manejarArgumento(String competidor, int valor) {
        argumentos.put(competidor, valor);
        System.out.println("Árbitro recibe argumento de " + competidor + ": " + valor);

        // SOLO 2 ARGUMENTOS
        if (argumentos.size() == 2) {
            System.out.println("\n=== Fase 3: Cálculo del ganador ===");

            calcularGanador();
        }
    }

    private void calcularGanador() {

        int oferta1 = ofertasFinales.get("competidor1");
        int oferta2 = ofertasFinales.get("competidor2");

        int arg1 = argumentos.get("competidor1");
        int arg2 = argumentos.get("competidor2");

        double y1 = oferta1 * 0.4 + arg1 * 0.6;
        double y2 = oferta2 * 0.4 + arg2 * 0.6;

        System.out.println("competidor1: Y = " + y1 + " (oferta=" + oferta1 + ", argumento=" + arg1 + ")");
        System.out.println("competidor2: Y = " + y2 + " (oferta=" + oferta2 + ", argumento=" + arg2 + ")");

        String ganador = (y1 > y2) ? "competidor1" :
                (y2 > y1) ? "competidor2" : "EMPATE";

        System.out.println("\n>>> GANADOR: " + ganador);

        enviarMensaje("competidor1", "GANADOR:" + ganador);
        enviarMensaje("competidor2", "GANADOR:" + ganador);
    }
}
