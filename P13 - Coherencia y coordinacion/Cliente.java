package dev.agents.kade;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.util.Random;

public class Cliente extends Agent {

    // ---------- Agent State ----------
    private Random random = new Random();
    private int proposal;
    private int amount = 0;

    // ---------- JADE Lifecycle ----------
    @Override
    protected void setup() {
        // say("Iniciado");

        proposal = random.nextInt(20, 50);

        say("Deseo comprar " + proposal + " unidades");

        ACLMessage offer = new ACLMessage(ACLMessage.INFORM);
        offer.addReceiver(new AID("Almacen", AID.ISLOCALNAME));
        offer.setContent("" + proposal);
        send(offer);

        if (blockingReceive().getPerformative() == ACLMessage.AGREE) {
            amount = proposal;
        }

        blockingReceive();

        say("He comprado " + amount + " unidades");

        doDelete();
    }

    @Override
    protected void takeDown() {
        // say("Terminado");
    }

    // ---------- Utilities ----------
    private void say(String msg) {
        System.out.println(getLocalName() + " => " + msg);
    }
}
