import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class AgenteCoordinador extends Agent {
    @Override
    protected void setup(){
        System.out.println("Agemte Coordinador: Iniciado" + getLocalName());
        ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
        mensaje.addReceiver(new AID("Sensor", AID.ISLOCALNAME));
        mensaje.setContent("Sensor, empieza a chambiar");
        send(mensaje);
        System.out.println("Coordinador: Envie mensaje al sensor");

        ACLMessage respuesta = blockingReceive();
        if (respuesta != null) {
            System.out.println("Coordinador: recibí -> " + respuesta.getContent());
        }



    }

}
