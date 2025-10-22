import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;

public class Validador extends Agent {
    @Override
    protected void setup() {
        System.out.println("Agente Validador iniciado ");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String item = msg.getContent();
                    boolean valido = item.toLowerCase().contains("rojo");

                    System.out.println("Validador: " + item + " es " + (valido ? "válido " : "inválido "));

                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(new AID("empaquetador", AID.ISLOCALNAME));
                    respuesta.setContent(item + ";" + valido);
                    send(respuesta);
                } else {
                    block();
                }
            }
        });
    }
}