import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.Map;

public class AgenteJuez extends Agent {

    private Map<String, Integer> calificaciones = new HashMap<>();
    private static final int NUM_COMPETIDORES = 4;

    @Override
    protected void setup() {
        System.out.println(  getLocalName() + " iniciado y esperando calificaciones...");

        while (calificaciones.size() < NUM_COMPETIDORES) {
            ACLMessage mensaje = blockingReceive();
            if (mensaje != null) {
                try {
                    String nombre = mensaje.getSender().getLocalName();
                    String contenido = mensaje.getContent().trim();
                    int calificacion = Integer.parseInt(contenido);

                    calificaciones.put(nombre, calificacion);
                    System.out.println(nombre + ": " + calificacion);
                } catch (NumberFormatException e) {
                    System.out.println(" Mensaje inválido recibido: " + mensaje.getContent());
                }
            }
        }

        // Determinar al ganador
        String ganador = null;
        int max = -1;

        for (Map.Entry<String, Integer> entry : calificaciones.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                ganador = entry.getKey();
            }
        }

        System.out.println("\n El ganador es " + ganador + " con una calificación de " + max);
    }
}
