import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.Random;

public class Sensor extends Agent {

    @Override
    protected void setup() {
        System.out.println("Agente Sensor iniciado: " + getLocalName());

        // Comportamiento para esperar mensaje AgenteCoordinador
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                // Esperar mensaje del AgenteCoordinador
                ACLMessage mensaje = receive();

                if (mensaje != null) {
                    String contenido = mensaje.getContent();
                    System.out.println("Mensaje recibido del Coordinador: " + contenido);


                    // Generar e imprimir temperatura
                    Random random = new Random();
                    int temperatura = random.nextInt(101); // de 0 a 100
                    System.out.println("Temperatura actual: " + temperatura + "°C");


                    // Enviar temperatura al Analizador
                    ACLMessage mensajeAnalizador = new ACLMessage(ACLMessage.INFORM);
                    mensajeAnalizador.addReceiver(new AID("Analizador", AID.ISLOCALNAME));
                    mensajeAnalizador.setContent("La temperatura es de: " + temperatura);
                    send(mensajeAnalizador);

                    System.out.println("Mensaje enviado al Analizador: La temperatura es de " + temperatura);
                } else {
                    block();
                }
            }
        });
    }
}


