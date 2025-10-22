import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class AgenteComprador extends Agent{
    @Override
    protected void setup(){
        System.out.println("Agente Comprador iniciado: " + getLocalName());

        int oferta = 50;
        int precioMax = 60;

        //Crear un mensaje
        ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
        mensaje.addReceiver(new AID("vendedor", AID.ISLOCALNAME));
        mensaje.setContent("Hola vendedor, quiero comprar un producto que valga " + oferta + " ¿Que ofreces?");

        //Enviar mensaje
        send(mensaje);
        System.out.println("Comprador: envié un mensaje al Vendedor.");

        // Espera un mensaje (bloqueante)
        ACLMessage respuesta = blockingReceive();
        if(respuesta != null){
            System.out.println("Comprador: recibí -> " + respuesta.getContent());
        }
    }
}
