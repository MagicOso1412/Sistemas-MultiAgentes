import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;


public class Pintor extends Agent {
    @Override
    protected void setup() {
        System.out.println("Agente Pintor iniciado ");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String item = msg.getContent();
                    String pintado = item + " pintado de rojo";

                    System.out.println("Pintor: recibí " + item + " y lo pinté de rojo.");

                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(new AID("validador", AID.ISLOCALNAME));
                    respuesta.setContent(pintado);
                    send(respuesta);
                } else {
                    block();
                }
            }
        });
    }
}
