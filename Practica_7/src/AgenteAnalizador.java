import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class AgenteAnalizador extends Agent{
    @Override
    protected void setup() {
        System.out.println("Agente Analizador iniciado: "+ getLocalName());
        ACLMessage mensaje = blockingReceive();
        if (mensaje != null) {
            System.out.println("El Agente Sensor midió-> " + mensaje.getContent()+"°C");
            String recepcion = mensaje.getContent();
            //La temperatura es:
            String temperatura = recepcion.substring(22, recepcion.length());
            ACLMessage mensajeReporte = new ACLMessage(ACLMessage.INFORM);
            mensajeReporte.addReceiver(new AID("Coordinador", AID.ISLOCALNAME));
            try {
                int realTemperatura = Integer.parseInt(temperatura);
                if (realTemperatura >= 0 && realTemperatura <= 50) {
                    mensajeReporte.setContent("Hace Frío");
                } else {
                    mensajeReporte.setContent("Hace Calor");
                }
                send(mensajeReporte);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}
