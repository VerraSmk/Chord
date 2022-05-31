package com.ssau.chord.model.node;



import com.ssau.chord.model.exceptions.TimerExpiredException;
import com.ssau.chord.model.exceptions.UnexpectedBehaviourException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Класс, который управляет обновлением атрибутов узла (predecessor, successor List,
 * finger Table), периодически вызывая методы узла (stabilize and fix Finger).
 * Класс также отвечает за оценку стабильности узла
 */
public class UpdateNode implements Runnable {
    private static Boolean active = true;
    private Node node;

    /**
     * @param node node to update
     */
    UpdateNode(Node node) {
        this.node = node;
    }

    /**
     * Called to stop the update when the node has disconnected
     */
    static void stopUpdate() {
        active = false;
    }

    /**
     * Поток, ответственный за периодический вызов node.stabilize() и node.fix Finger(),
     * для оценки стабильности узла на основе: проверка, изменен ли successor list
     * и finger table.
     */
    @Override
    public void run() {
        boolean stable = false;
        while (active) {
            if (node.getPredecessor() != null) {
                //Get Old List Value to be compared at the end
                ArrayList<Long> oldSuccessorList = new ArrayList<>();
                for (NodeInterface n : node.getSuccessorList())
                    oldSuccessorList.add(n.getNodeId());
                try {
                    node.listStabilize();
                } catch (TimerExpiredException e) {
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } catch (IOException e) {
                    throw new UnexpectedBehaviourException();
                }

                ArrayList<Long> newSuccessorList = new ArrayList<>();
                for (NodeInterface n : node.getSuccessorList())
                    newSuccessorList.add(n.getNodeId());

                stable = oldSuccessorList.equals(newSuccessorList); //The Order matters

                oldSuccessorList.clear();
                newSuccessorList.clear();
            }

            // get old Finger Table
            ArrayList<Long> oldFingerTableList = new ArrayList<>();
            for (Map.Entry<Integer, NodeInterface> entry : node.getFingerTable().entrySet()) {
                oldFingerTableList.add(entry.getValue().getNodeId());
            }
            for (int i = 0; i < node.getDimFingerTable(); i++) {
                try {
                    node.fixFingers();
                    Thread.sleep(200);
                } catch (IOException e) {
                    throw new UnexpectedBehaviourException();
                } catch (TimerExpiredException e) {
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            ArrayList<Long> newFingerTableList = new ArrayList<>();
            for (Map.Entry<Integer, NodeInterface> entry : node.getFingerTable().entrySet())
                newFingerTableList.add(entry.getValue().getNodeId());
            stable = stable && oldFingerTableList.equals(newFingerTableList);  //The Order matters
            oldFingerTableList.clear();
            newFingerTableList.clear();
            try {
                node.updateStable(stable);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
