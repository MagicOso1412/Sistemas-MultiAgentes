import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class AgenteAdministrador extends Agent {
    private List<String> trabajadores = Arrays.asList("trabajador1","trabajador2","trabajador3");
    private Map<String,Integer> resultados = new HashMap<>();
    @Override
    protected void setup() {
        System.out.println("Agente Administrador Inicializado: " + getLocalName());
        int it=20;
        Random random = new Random();
        do{
            int costoChamba = random.nextInt(10);
            ACLMessage mensajeArranque = new ACLMessage(ACLMessage.REQUEST);
            for (String trabajador : trabajadores) {
                mensajeArranque.addReceiver(new AID(trabajador,AID.ISLOCALNAME));
            }
            mensajeArranque.setContent("Esta tarea cuesta " + costoChamba);
            System.out.println("La tarea cuesta: " + costoChamba);
            send(mensajeArranque);
            int respuestasEsperadas = trabajadores.size();
            int respuestasRecibidas = 0;
            while(respuestasEsperadas != respuestasRecibidas){
                ACLMessage mensaje = blockingReceive();
                if (mensaje != null){
                    String cargo = mensaje.getSender().getLocalName();
                    String contenido = mensaje.getContent();
                    System.out.println(cargo+" : "+contenido);
                    String contenidoPercibido = contenido.substring(15,contenido.length());
                    int aptitud = Integer.parseInt(contenidoPercibido);
                    resultados.put(cargo, aptitud);
                }
                respuestasRecibidas++;
            }
            String mejorAptitud = trabajadores.get(random.nextInt(trabajadores.size()));
            int mejorResultado = 0;
            int sobrecargados = 0;
            for(String trabajador: trabajadores){
                    if(resultados.get(trabajador) > mejorResultado){
                        mejorAptitud = trabajador;
                        mejorResultado = resultados.get(trabajador);
                    }
                    if(resultados.get(trabajador) == -100000000){
                        sobrecargados++;
                    }
            }
            if (sobrecargados == trabajadores.size()){
                System.out.println("Se omite la tarea");
            }else{
                System.out.println("El más capacitado es: "+mejorAptitud+" : "+mejorResultado);
                ACLMessage mensajeConfirmacion = new ACLMessage(ACLMessage.AGREE);
                mensajeConfirmacion.addReceiver(new AID(mejorAptitud,AID.ISLOCALNAME));
                mensajeConfirmacion.setContent("Cuesta: "+costoChamba);
                send(mensajeConfirmacion);
                ACLMessage esperanza = blockingReceive();
                if (esperanza != null){
                    System.out.println("El agente terminó su tarea");
                }
            }
        }while(it-->0);



    }
}
