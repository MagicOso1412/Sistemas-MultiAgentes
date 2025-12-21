package dev.agents.kade;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.util.Random;

public class Almacen extends Agent {

    // ---------- Agent State ----------
    private Random random = new Random();
    private int stock;

    // ---------- JADE Lifecycle ----------
    @Override
    protected void setup() {
        // say("Iniciado");

        stock = random.nextInt(80, 120);

        say("Mi inventario inicial es: " + stock);

        for (int i = 0; i < 3; i++) {
            ACLMessage proposal = blockingReceive();
            int amount = Integer.parseInt(proposal.getContent());

            if (stock > amount) {
                stock -= amount;

                say(
                    "Acepto la compra del " +
                        proposal.getSender().getLocalName()
                );
                ACLMessage accept = new ACLMessage(ACLMessage.AGREE);
                accept.addReceiver(proposal.getSender());
                send(accept);
            } else {
                say(
                    "Rechazo la compra del " +
                        proposal.getSender().getLocalName()
                );
                ACLMessage reject = new ACLMessage(ACLMessage.CANCEL);
                reject.addReceiver(proposal.getSender());
                send(reject);
            }
        }

        say("Mi inventario restante es: " + stock);

        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        for (int i = 0; i < 3; i++) {
            reply.addReceiver(new AID("Cliente" + (i + 1), AID.ISLOCALNAME));
        }
        send(reply);

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
