import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jdk.swing.interop.SwingInterOpUtils;

import java.util.Random;

public class AgenteMecanico extends Agent {

    private int generarNumero() {
        Random random = new Random();
        return random.nextInt(2);
    }

    @Override
    protected void setup(){
        // Inicializar agente.
        System.out.println("Agente Mecanico Iniciado: " + getLocalName());

        ACLMessage solicitud = blockingReceive();
        if (solicitud != null) {
            System.out.println("Mecanico: recibí -> " + solicitud.getContent());
            switch (generarNumero()) {
                case 0:
                    ACLMessage respuesta = new ACLMessage(ACLMessage.AGREE);
                    respuesta.addReceiver(new AID("coordinador", AID.ISLOCALNAME));
                    respuesta.setContent("Acepto humildemente la tarea solicitada");
                    send(respuesta);
                    System.out.println("Mecanico: envié un mensaje a el Coordinador");


                    switch (generarNumero()) {
                        case 0:
                            ACLMessage respuesta3 = new ACLMessage(ACLMessage.INFORM);
                            respuesta3.addReceiver(new AID("coordinador", AID.ISLOCALNAME));
                            respuesta3.setContent("\"Repare el dron con exito\"");
                            send(respuesta3);
                            break;
                        case 1:
                            ACLMessage respuesta4 = new ACLMessage(ACLMessage.FAILURE);
                            respuesta4.addReceiver(new AID("coordinador", AID.ISLOCALNAME));
                            respuesta4.setContent("\"Falle al reparar el dron.\"");
                            send(respuesta4);
                            break;
                    }
                    break;
                case 1:
                    ACLMessage respuesta2 = new ACLMessage(ACLMessage.REFUSE);
                    respuesta2.addReceiver(new AID("coordinador", AID.ISLOCALNAME));
                    respuesta2.setContent("Mecanico Rechazo la solicitud de la tarea");
                    send(respuesta2);
                    System.out.println("Mecanico: envié un mensaje a el Coordinador");
                    break;
            }
        }

    }
}
