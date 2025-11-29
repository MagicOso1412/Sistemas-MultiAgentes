import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.Behaviour;
import java.util.Random;

public class AgenteSupervisor extends Agent {

    private int min = 40;
    private int max = 60;
    private int iteraciones = 0;

    private int generarNumero() {
        Random random = new Random();
        return random.nextInt(101);
    }

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " Agente Supervisor Iniciado.");

        addBehaviour(new Behaviour() {
            @Override
            public void action() {
                if (iteraciones >= 20) {
                    System.out.println(getLocalName() + ": He terminado mis 20 iteraciones.");
                    doDelete();
                    return;
                }

                int bateria = generarNumero();
                System.out.println(getLocalName() + " batería actual: " + bateria + "% (iteración " + (iteraciones + 1) + ")");

                // Enviar mensaje al Productor
                ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
                mensaje.addReceiver(new AID("Agente Productor", AID.ISLOCALNAME));
                mensaje.setContent(String.valueOf(bateria));
                send(mensaje);

                // Esperar respuesta del Consumidor o Productor
                ACLMessage respuesta = blockingReceive(2000); // espera 2 segundos
                if (respuesta != null) {
                    System.out.println(getLocalName() + " recibió -> " + respuesta.getContent());
                } else {
                    System.out.println(getLocalName() + " no recibió respuesta en esta iteración.");
                }

                iteraciones++;
            }

            @Override
            public boolean done() {
                return iteraciones >= 20;
            }
        });
    }
}
