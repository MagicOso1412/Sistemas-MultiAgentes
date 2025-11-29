import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.LinkedList;
import java.util.Random;

public class AgenteTrabajador extends Agent {
    private LinkedList<Integer> tareas = new LinkedList<>();
    @Override
    protected void setup() {
        Random rand = new Random();
        int carga = 0;
        int capacidad = 10;
        System.out.println("Agente Trabajador Inicializado: " + getLocalName() + " con una capacidad: " + capacidad);
        while(true){
            ACLMessage msg = blockingReceive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.AGREE) {
                    String contenido = msg.getContent();
                    String numeroCosto = contenido.substring(8,contenido.length());
                    int costo = Integer.parseInt(numeroCosto);
                    tareas.add(costo);
                    System.out.println("Su carga es de: "+tareas.size());
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Su carga es de: "+tareas.size());
                    send(reply);
                }else{
                    if(tareas.isEmpty()){
                        String contenido = msg.getContent();
                        String numeroCosto = contenido.substring(18,contenido.length());
                        int costo = Integer.parseInt(numeroCosto);
                        int aptitud = capacidad * 2-(carga*3)-costo;
                        System.out.println(getLocalName()+": tengo una aptitud de: ("+capacidad+ "*2)-("+carga+"*3)-("+costo+")="+aptitud);
                        System.out.print(getLocalName()+":Tengo " +tareas.size()+" tareas y estas se estan haciendose ->");
                        for(Integer tarea : tareas){
                            System.out.print(tarea);
                        }
                        System.out.println();
                        ACLMessage respuesta = msg.createReply();
                        respuesta.setPerformative(ACLMessage.INFORM);
                        respuesta.setContent("Mi aptitud es: "+aptitud);
                        send(respuesta);
                    }else{
                        if(tareas.getFirst() == 0 ){
                           tareas.removeFirst();
                            carga = tareas.size();
                            capacidad = 10 - carga;
                            String contenido = msg.getContent();
                            String numeroCosto = contenido.substring(18,contenido.length());
                            int costo = Integer.parseInt(numeroCosto);
                            int aptitud = capacidad * 2-(carga*3)-costo;
                            System.out.println(getLocalName()+": tengo una aptitud de: ("+capacidad+ "*2)-("+carga+"*3)-("+costo+")="+aptitud);
                            System.out.print(getLocalName()+":Tengo " +tareas.size()+" tareas y estas se estan haciendose ->");
                            for(Integer tarea : tareas){
                                System.out.print(tarea);
                            }
                            System.out.println();
                            ACLMessage respuesta = msg.createReply();
                            respuesta.setPerformative(ACLMessage.INFORM);
                            respuesta.setContent("Mi aptitud es: "+aptitud);
                            send(respuesta);
                        }else{
                            int nuevo = tareas.getFirst();
                            tareas.set(0, nuevo-1);
                            carga = tareas.size();
                            capacidad = 10 - carga;
                            String contenido = msg.getContent();
                            String numeroCosto = contenido.substring(18,contenido.length());
                            int costo = Integer.parseInt(numeroCosto);
                            int aptitud;
                            if (carga == 10){
                                aptitud = -100000000;
                                System.out.println("Estoy sobrecargado");
                            }else{
                                aptitud = capacidad * 2-(carga*3)-costo;
                                System.out.println(getLocalName()+": tengo una aptitud de: ("+capacidad+ "*2)-("+carga+"*3)-("+costo+")="+aptitud);
                                System.out.print(getLocalName()+":Tengo " +tareas.size()+" tareas y estas se estan haciendose ->");
                                for(Integer tarea : tareas){
                                    System.out.print(tarea);
                                    System.out.print(" ");
                                }
                                System.out.println();
                            }
                            ACLMessage respuesta = msg.createReply();
                            respuesta.setPerformative(ACLMessage.INFORM);
                            respuesta.setContent("Mi aptitud es: "+aptitud);
                            send(respuesta);
                        }
                    }

                }
            }
        }

    }
}
