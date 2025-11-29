import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

public class AgenteAdmin extends Agent {

    // Función para obtener SOLO el nombre del ganador
    private String obtenerGanador(Map<String, Integer> mapaAptitudes) {
        String ganador = null;
        int max = Integer.MIN_VALUE;

        for (Map.Entry<String, Integer> entry : mapaAptitudes.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                ganador = entry.getKey();
            }
        }
        return ganador;
    }

    @Override
    protected void setup(){
        System.out.println(getLocalName() + " Agente Administrador Iniciado.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                String[] agentes = {"traba1","traba2","traba3"};

                // Enviar petición de aptitud
                for (String a : agentes) {
                    ACLMessage peticion = new ACLMessage(ACLMessage.REQUEST);
                    peticion.addReceiver(new AID(a, AID.ISLOCALNAME));
                    peticion.setContent("Enviar aptitud");
                    send(peticion);
                    System.out.println("Administrador: envié mensaje a " + a);
                }

                int respuestasEsperadas = agentes.length;
                int respuestasRecibidas = 0;

                // Guardar aptitudes
                Map<String, Integer> aptitudes = new HashMap<>();

                while (respuestasRecibidas < respuestasEsperadas){
                    ACLMessage respuesta = blockingReceive();
                    if (respuesta != null){
                        String emisor = respuesta.getSender().getLocalName();
                        int aptitud = Integer.parseInt(respuesta.getContent());

                        aptitudes.put(emisor, aptitud);
                        respuestasRecibidas++;

                        System.out.println("Recibido: " + emisor + " → aptitud " + aptitud);
                    }
                }

                // Obtener al ganador real
                String ganador = obtenerGanador(aptitudes);
                int aptitudGanador = aptitudes.get(ganador);

                System.out.println("\n El ganador es: " + ganador + " con aptitud " + aptitudGanador);

                // Enviar tarea = 1 al ganador
                ACLMessage tarea = new ACLMessage(ACLMessage.INFORM);
                tarea.addReceiver(new AID(ganador, AID.ISLOCALNAME));
                tarea.setContent("1");  // valor de tarea
                send(tarea);

                System.out.println("Administrador: envié la tarea = 1 a " + ganador + "\n");
            }
        });
    }
}
