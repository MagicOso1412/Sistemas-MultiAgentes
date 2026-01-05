
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class UIAgent extends Agent {

    private Escenario escenario;
    private AID worldAid;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " listo (UIAgent).");

        // Por defecto asume que el World se llama "world"
        worldAid = new AID("world", AID.ISLOCALNAME);

        // Suscribirse al mundo
        ACLMessage sub = new ACLMessage(ACLMessage.SUBSCRIBE);
        sub.addReceiver(worldAid);
        sub.setContent("ui");
        send(sub);

        // Abrir GUI
        escenario = new Escenario();
        escenario.setVisible(true);

        // Cuando se presiona Run: manda mapa al World
        escenario.setRunListener(matrix -> {
            try {
                ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
                inf.addReceiver(worldAid);
                inf.setConversationId("MAP_INIT");
                inf.setContentObject(matrix);
                send(inf);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        });

        // Recibe estado para renderizar
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) { block(); return; }
                if (msg.getPerformative() == ACLMessage.INFORM &&
                        "STATE_UPDATE".equals(msg.getConversationId())) {
                    try {
                        GameState s = (GameState) msg.getContentObject();
                        escenario.render(s);
                    } catch (Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        });
    }
}
