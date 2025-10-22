import jade.core.Agent;

public class Agente extends Agent {
    @Override
    protected void setup() {
        System.out.println("Hola, soy el agente: " + getLocalName());
    }
}
