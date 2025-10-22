import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class AgenteEmisor extends Agent{
    @Override
    protected void setup(){
        System.out.println("Agente Emisor iniciado: " + getLocalName());

        //Crear un mensaje
        ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
        mensaje.addReceiver(new AID("receptor", AID.ISLOCALNAME));
        mensaje.setContent("Hola Receptor, ¿Cómo estás?");

        //Enviar mensaje
        send(mensaje);
        System.out.println("Emisor: envié un mensaje al Receptor.");
    }
}
