import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class AgenteTraba1 extends Agent {

    private int generarNumero() {
        Random random = new Random();
        return random.nextInt(3) + 1;
    }

    private int generarCapacidad() {
        Random random = new Random();
        return random.nextInt(10) + 1;
    }

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " Agente Traba1 Iniciado.");

        addBehaviour(new CyclicBehaviour() {

            int iteraciones = 0;

            @Override
            public void action() {

                // Si ya hizo 5 ciclos → FIN
                if (iteraciones >= 5) {
                    System.out.println("Traba1: Finalizando después de 5 iteraciones.");
                    myAgent.doDelete();
                    return;
                }

                ACLMessage mensaje = blockingReceive();

                if (mensaje != null) {
                    String contenido = mensaje.getContent();
                    String emisor = mensaje.getSender().getLocalName();

                    System.out.println("Traba1: recibí -> " + contenido + " de " + emisor);

                    // Solo si el mensaje es "Enviar aptitud" cuenta como iteración
                    if (contenido.equalsIgnoreCase("Enviar aptitud")) {

                        // Calcular aptitud
                        int capacidad = generarCapacidad();
                        int carga = generarNumero();
                        int costo = generarNumero();
                        int aptitud = (2 * capacidad) - (3 * carga) - costo;

                        System.out.println("Traba1: Aptitud = (2 * " + capacidad
                                + ") - (3 * " + carga + ") - " + costo + " = " + aptitud);

                        // Responder al Administrador
                        ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                        respuesta.addReceiver(new AID("AgenteAdmin", AID.ISLOCALNAME));
                        respuesta.setContent(String.valueOf(aptitud));
                        send(respuesta);

                        iteraciones++;
                    }


                    else if (contenido.equals("1")) {
                        System.out.println("Traba1: Recibí tarea = 1. Ejecutando tarea...");
                    }
                }
            }
        });

    }
}
