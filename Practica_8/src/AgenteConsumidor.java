import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class AgenteConsumidor extends Agent {
    private int min = 40;
    private int max = 60;
    private int contador = 0;

    @Override
    protected void setup() {
        System.out.println("Agente Consumidor Iniciado: " + getLocalName());

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                if (contador >= 20) {
                    System.out.println("Agente Consumidor: completó sus 20 ciclos. Terminando...");
                    doDelete();
                    return;
                }

                ACLMessage mensaje = blockingReceive();
                if (mensaje != null) {
                    int bateria = Integer.parseInt(mensaje.getContent());
                    System.out.println("Agente Consumidor recibió -> " + bateria + "%");

                    int nuevaCarga;

                    if (bateria > max) {
                        System.out.println("Agente Consumidor: batería alta (" + bateria + "%), descargando");
                        nuevaCarga = bateria - 10;
                    } else if (bateria >= min && bateria <= max) {
                        System.out.println("Agente Consumidor: batería dentro del umbral (" + bateria + "%), descargando");
                        nuevaCarga = bateria - 10;
                    } else {
                        System.out.println("Agente Consumidor: batería baja (" + bateria + "%), no descarga.");
                        nuevaCarga = bateria;
                    }

                    // Enviar respuesta al supervisor
                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(new AID("Agente Supervisor", AID.ISLOCALNAME));
                    respuesta.setContent(String.valueOf(nuevaCarga));
                    send(respuesta);

                    System.out.println(getLocalName() + " envió su % de batería (" + nuevaCarga + "%) al Supervisor.\n");
                    contador++;
                } else {
                    block();
                }
            }
        });
    }
}
