import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.OneShotBehaviour;

public class Productor extends Agent {
    @Override
    protected void setup() {
        System.out.println("Agente Productor iniciado ");

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                // Espera 2 segundos para que los demás agentes arranquen
                doWait(2000);

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("pintor", AID.ISLOCALNAME));
                msg.setContent("Juguete1");
                send(msg);

                System.out.println("Productor: Creé Juguete1 y lo envié al Pintor.");
            }
        });
    }
}