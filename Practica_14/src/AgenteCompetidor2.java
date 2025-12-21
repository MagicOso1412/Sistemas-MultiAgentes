
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class AgenteCompetidor2 extends Agent {

    private int ofertaActual;
    private int ronda = 1;
    private boolean argumentoEnviado = false;
    private final Random random = new Random();

    @Override
    protected void setup() {
        System.out.println("Competidor2 inicializado: " + getLocalName());

        // FASE 1: Enviar oferta inicial
        addBehaviour(new jade.core.behaviours.OneShotBehaviour() {
            @Override
            public void action() {
                ofertaActual = random.nextInt(101); // 0-100
                enviarOferta();
            }
        });

        // Comportamiento principal
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();

                if (msg != null) {
                    String contenido = msg.getContent();
                    String[] partes = contenido.split(":");

                    if (partes[0].equals("OFERTAS")) {
                        manejarOfertasRivales(partes);
                    } else if (partes[0].equals("GANADOR")) {
                        System.out.println(getLocalName() + " recibe GANADOR = " + partes[1]);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void enviarOferta() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("arbitro", AID.ISLOCALNAME));
        msg.setContent("OFERTA:" + ofertaActual);
        send(msg);

        System.out.println(getLocalName() + " envía oferta: " + ofertaActual);
    }

    private void manejarOfertasRivales(String[] partes) {
        // Formato: OFERTAS:<ofertaCompetidor1>:<ofertaCompetidor2>
        int ofertaComp1 = Integer.parseInt(partes[1]);
        int ofertaComp2 = Integer.parseInt(partes[2]);

        int mayor = Math.max(ofertaComp1, ofertaComp2);

        System.out.println(getLocalName() + " recibe ofertas -> comp1=" +
                ofertaComp1 + ", comp2=" + ofertaComp2 +
                " (mi oferta actual = " + ofertaActual + ")");

        if (ronda < 3) {
            if (ofertaActual < mayor) {
                int margen = 100 - ofertaActual;
                int incremento = margen > 0 ? random.nextInt(margen + 1) : 0;
                ofertaActual = ofertaActual + incremento;
                System.out.println(getLocalName() + " SUBE su oferta a " + ofertaActual);
            } else {
                System.out.println(getLocalName() + " MANTIENE su oferta en " + ofertaActual);
            }

            ronda++;
            enviarOferta();

        } else {
            if (!argumentoEnviado) {
                int argumento = random.nextInt(101);
                System.out.println(getLocalName() + " envía argumento: " + argumento);

                ACLMessage argMsg = new ACLMessage(ACLMessage.INFORM);
                argMsg.addReceiver(new AID("arbitro", AID.ISLOCALNAME));
                argMsg.setContent("ARG:" + argumento);
                send(argMsg);

                argumentoEnviado = true;
            }
        }
    }
}
