import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.util.Random;

public class AgenteCom3 extends Agent {

    private int generarNumero() {
        Random random = new Random();
        return random.nextInt(10) + 1;
    }

    @Override
    protected void setup() {
        int calificacion = generarNumero();
        System.out.println(getLocalName() + " iniciado. Calificación: " + calificacion);

        ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
        mensaje.addReceiver(new AID("AgenteJuez", AID.ISLOCALNAME));
        mensaje.setContent(String.valueOf(calificacion));

        send(mensaje);
        System.out.println(getLocalName() + " envió su calificación al juez.");
    }
}
