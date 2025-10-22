import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class AgenteReceptor extends Agent{
    @Override
    protected void setup(){
        System.out.println("Agente Receptor iniciado: " + getLocalName());

        // Espera un mensaje (bloqueante)
        ACLMessage mensaje = blockingReceive();
        if(mensaje != null){
            System.out.println("Receptor: recibí -> " + mensaje.getContent());

            //Responder al emisor
            ACLMessage respuesta = mensaje.createReply();
            respuesta.setPerformative(ACLMessage.INFORM);
            respuesta.setContent("Hola Emisor, estoy bien. ¡Gracias!");
            send(respuesta);

            System.out.println("Receptor: respondi al Emisor.");
        }
    }
}
