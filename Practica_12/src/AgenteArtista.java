import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class AgenteArtista extends Agent {

    private List<String> compradores;        // nombres locales de los compradores
    private List<String> activos;            // compradores que siguen en la subasta
    private int precioActual;
    private int incremento = 10;

    @Override
    protected void setup() {
        System.out.println("Agente Artista inicializado: " + getLocalName());

        // Nombres locales de los compradores (así los lanzarás en la consola de JADE)
        compradores = Arrays.asList("comprador1", "comprador2", "comprador3", "comprador4");
        activos = new ArrayList<>(compradores);

        // Precio inicial aleatorio
        Random rnd = new Random();
        precioActual = 50 + rnd.nextInt(51); // 50-100
        System.out.println("Precio inicial de la subasta: " + precioActual);

        addBehaviour(new SubastaBehaviour());
    }

    private class SubastaBehaviour extends CyclicBehaviour {

        private int ofertasRecibidas = 0;
        private int mejorOferta = -1;
        private String mejorPostor = null;
        private Set<String> retiradosEstaRonda = new HashSet<>();

        private void iniciarRonda() {
            System.out.println("\n=== Nueva ronda. Precio actual: " + precioActual + " ===");
            ofertasRecibidas = 0;
            mejorOferta = -1;
            mejorPostor = null;
            retiradosEstaRonda.clear();

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (String c : activos) {
                cfp.addReceiver(new AID(c, AID.ISLOCALNAME));
            }
            cfp.setContent("PRECIO:" + precioActual);
            myAgent.send(cfp);
        }

        @Override
        public void onStart() {
            iniciarRonda();
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                String sender = msg.getSender().getLocalName();

                switch (msg.getPerformative()) {
                    case ACLMessage.PROPOSE: {
                        ofertasRecibidas++;
                        String[] parts = msg.getContent().split(":");
                        int oferta = Integer.parseInt(parts[1]);
                        System.out.println(sender + " ofrece " + oferta);

                        if (oferta > mejorOferta) {
                            mejorOferta = oferta;
                            mejorPostor = sender;
                        }
                        break;
                    }
                    case ACLMessage.REFUSE: {
                        ofertasRecibidas++;
                        System.out.println(sender + " se retira de la subasta.");
                        retiradosEstaRonda.add(sender);
                        break;
                    }
                    default:
                        // ignoramos otros tipos de mensaje
                        break;
                }

                // ¿Ya contestaron todos los compradores activos en esta ronda?
                if (ofertasRecibidas == activos.size()) {
                    // Actualizar lista de activos eliminando retirados
                    activos.removeAll(retiradosEstaRonda);

                    if (mejorPostor == null) {
                        System.out.println("Nadie hizo oferta. Subasta terminada sin ganador.");
                        myAgent.doDelete();
                        return;
                    }

                    if (activos.size() == 1) {
                        // Solo queda un comprador, es el ganador
                        System.out.println("\n*** Ganador de la subasta: " + mejorPostor +
                                " por " + mejorOferta + " ***");

                        ACLMessage acept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        acept.addReceiver(new AID(mejorPostor, AID.ISLOCALNAME));
                        acept.setContent("GANASTE:" + mejorOferta);
                        myAgent.send(acept);

                        ACLMessage fin = new ACLMessage(ACLMessage.INFORM);
                        for (String c : compradores) {
                            if (!c.equals(mejorPostor)) {
                                fin.addReceiver(new AID(c, AID.ISLOCALNAME));
                            }
                        }
                        fin.setContent("SUBASTA_TERMINADA");
                        myAgent.send(fin);

                        myAgent.doDelete();
                    } else {
                        // Sigue habiendo competencia → nueva ronda
                        precioActual = mejorOferta + incremento;
                        iniciarRonda();
                    }
                }

            } else {
                block();
            }
        }
    }
}
