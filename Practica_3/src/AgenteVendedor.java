import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class AgenteVendedor extends Agent{
    @Override
    public void setup(){
        System.out.println("Agente Vendedor iniciado: " + getLocalName());

        int oferta = 90;

        // Espera un mensaje (bloqueante)
        ACLMessage mensaje = blockingReceive();
        if(mensaje != null){
            System.out.println("Vendedor: recibí -> " + mensaje.getContent());

            //Responder al emisor
            ACLMessage respuesta = mensaje.createReply();
            respuesta.setPerformative(ACLMessage.INFORM);
            respuesta.setContent("Hola Comprador, te ofrezco una bolsa de aire a " + oferta);
            send(respuesta);

            System.out.println("Vendedor: respondi al Comprador.");


        }
    }
}
