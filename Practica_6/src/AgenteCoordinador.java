import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class AgenteCoordinador extends Agent {

    @Override
    protected void setup() {
        // Inicializar agente.
        System.out.println("Agente Coordinador Iniciado: " + getLocalName());

        String[] tecnicos = {"mecanico", "electrico", "software"};

        //Crear Mensaje
        for (String t : tecnicos) {
            ACLMessage solicitud = new ACLMessage(ACLMessage.REQUEST);
            solicitud.addReceiver(new AID(t, AID.ISLOCALNAME));
            solicitud.setContent("Reparar dron - Modulo asignado");
            send(solicitud);
            System.out.println("Coordinador: envié un mensaje a los tecnicos");
        }

        // Esperar Mensaje
        int respuestasEsperadas = tecnicos.length*2;
        int respuestasRecibidas = 0;
        int exitos = 0;

        while (respuestasRecibidas < respuestasEsperadas){
            ACLMessage respuesta = blockingReceive();
            if (respuesta != null){
                System.out.println("Coordinador: recibí -> "+ respuesta.getContent());
                respuestasRecibidas++;
                String emisor = respuesta.getSender().getLocalName();
                String contenido = respuesta.getContent();

                switch (respuesta.getPerformative()){
                    case ACLMessage.AGREE:
                        System.out.println("" + emisor + ": Acepto la tarea");

                        ACLMessage resultado = blockingReceive();
                        if (resultado != null) {
                            switch (resultado.getPerformative()) {
                                case ACLMessage.INFORM:
                                    System.out.println("" + emisor + ": Completó la tarea con éxito");
                                    break;
                                case ACLMessage.FAILURE:
                                    System.out.println("" + emisor + ": Falló la tarea");
                                    break;
                            }
                            respuestasRecibidas++; // cuenta el segundo mensaje
                        }
                        break;

                    case ACLMessage.REFUSE:
                        System.out.println("" + emisor + ": Rechazo la tarea");
                        break;

                }
            }
        }


    }
}
