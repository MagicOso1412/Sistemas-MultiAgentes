import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class AgenteProductor extends Agent {
    private int min = 40;
    private int max = 60;

    @Override
    protected void setup() {
        System.out.println("Agente Productor Iniciado: " + getLocalName());

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage mensaje = blockingReceive();
                if (mensaje != null) {
                    System.out.println("Agente Productor recibió -> " + mensaje.getContent());
                    int bateria = Integer.parseInt(mensaje.getContent());
                    int carga;

                    if (bateria < min) {
                        System.out.println("Agente Productor: batería baja (" + bateria + "%), cargando bateria");
                        carga = bateria + 10;
                    } else if (bateria > max) {
                        System.out.println("Agente Productor: batería alta (" + bateria + "%), sin necesidad de carga.");
                        carga = bateria;
                    } else {
                        System.out.println("Agente Productor: batería dentro del umbral (" + bateria + "%).");
                        carga = bateria;
                    }

                    // Enviar al consumidor
                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(new AID("Agente Consumidor", AID.ISLOCALNAME));
                    respuesta.setContent(String.valueOf(carga));
                    send(respuesta);

                    System.out.println(getLocalName() + " envió su % de batería (" + carga + "%) al Consumidor.\n");
                } else {
                    block();
                }
            }
        });
    }
}
