import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class Empaquetador extends Agent {
    @Override
    protected void setup() {
        System.out.println("Agente Empaquetador iniciado ");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String[] partes = msg.getContent().split(";");
                    String item = partes[0];
                    boolean valido = Boolean.parseBoolean(partes[1]);

                    if (valido) {
                        System.out.println("Empaquetador: " + item + " empaquetado ");
                    } else {
                        System.out.println("Empaquetador: " + item + " rechazado ");
                    }
                } else {
                    block();
                }
            }
        });
    }
}