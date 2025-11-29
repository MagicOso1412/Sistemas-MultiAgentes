import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class AgenteComprador extends Agent {

    private int presupuesto;
    private boolean activo = true;
    private Random rnd = new Random();

    @Override
    protected void setup() {
        presupuesto = 80 + rnd.nextInt(121); // 80-200
        System.out.println("Agente " + getLocalName() +
                " inicializado con presupuesto: " + presupuesto);

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    switch (msg.getPerformative()) {
                        case ACLMessage.CFP:
                            manejarCFP(msg);
                            break;
                        case ACLMessage.ACCEPT_PROPOSAL:
                            System.out.println(getLocalName() + " recibió ACCEPT_PROPOSAL. " +
                                    "Mensaje: " + msg.getContent());
                            System.out.println(getLocalName() + " GANÓ la subasta 🎉");
                            doDelete();
                            break;
                        case ACLMessage.INFORM:
                            System.out.println(getLocalName() +
                                    " recibió fin de subasta. Mensaje: " + msg.getContent());
                            doDelete();
                            break;
                        default:
                            break;
                    }
                } else {
                    block();
                }
            }

            private void manejarCFP(ACLMessage msg) {
                if (!activo) return;

                String[] parts = msg.getContent().split(":");
                int precio = Integer.parseInt(parts[1]);
                System.out.println(getLocalName() + " recibe CFP con precio " + precio);

                if (precio > presupuesto) {
                    // No puedo seguir, me retiro
                    ACLMessage refuse = msg.createReply();
                    refuse.setPerformative(ACLMessage.REFUSE);
                    refuse.setContent("ME_RETIRO");
                    myAgent.send(refuse);
                    activo = false;
                    System.out.println(getLocalName() +
                            " se retira (presupuesto " + presupuesto + " insuficiente).");
                } else {
                    // Hago una oferta entre [precio, presupuesto]
                    int ofertaMin = precio;
                    int ofertaMax = presupuesto;
                    int rango = ofertaMax - ofertaMin;

                    int oferta;
                    if (rango <= 0) {
                        oferta = ofertaMin; // solo alcanzas el precio actual
                    } else {
                        oferta = ofertaMin + rnd.nextInt(rango + 1);
                    }

                    ACLMessage prop = msg.createReply();
                    prop.setPerformative(ACLMessage.PROPOSE);
                    prop.setContent("OFERTA:" + oferta);
                    myAgent.send(prop);

                    System.out.println(getLocalName() +
                            " hace oferta de " + oferta + " (presupuesto " + presupuesto + ")");
                }
            }
        });
    }
}
