package com.ssau.chord.model.controller;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

/**
 * Одноэлементный класс, отвечающий за создание сетевого контроллера.
 * Он использует карту с идентификатором узла в качестве ключа и NodeInfo как ключ.
 * Таким образом, мы можем собрать всю информацию, необходимую для измерения времени,
 * прошедшего между началом операций, 'insert Key', 'lookup', 'find Key, и их фактическое
 * время окончания. Кроме того, класс несет ответственность для подсчета количества узлов,
 * присутствующих в сети аккордов, и способен измерять время конвергенции между
 *  двумя последовательными присоединениями или выходами узла из самой сети.
 *  Все методы вызываются классом 'Controller Communicator', который имеет соответствующие
 *  методы, которые запускаются при поступлении сообщения определенного типа.
 */
public class Controller {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";

    private static Controller controllerInstance;
    private LocalTime startTimeStability;
    private volatile Map<Long, NodeInfo> nodeMap;

    private Controller() {
        this.nodeMap = new LinkedHashMap<>();
    }

    /**
     * The singleton design pattern is applied
     * @return The singleton object
     */
    public static Controller getController() {
        if (controllerInstance == null)
            controllerInstance = new Controller();
        return controllerInstance;
    }

    /**
     * Нанесите на карту новый узел сети.
     * Время начала конвергенции сбрасывается
     * @param nodeId of the entering new node
     */
    synchronized void newConnection(Long nodeId) {
        nodeMap.put(nodeId, new NodeInfo());
        this.startTimeStability = LocalTime.now();
        if (nodeMap.size() == 1)
            updateStable(nodeId, true);
        out.println("New Connection: " + nodeId + ". Number of connected node: " + nodeMap.size());
    }

    /**
     * The start time of the convergence is reset. The node with the specific Id is removed from the map.
     *
     * @param nodeId of the node that is exiting for the network
     */
    synchronized void disconnectedNode(Long nodeId) {
        nodeMap.remove(nodeId);
        this.startTimeStability = LocalTime.now();
        out.println("Disconnected node: " + nodeId + ". Number of connected node: " + nodeMap.size());
    }

    /**
     * Каждый раз, когда приходит сообщение о времени Stable Message' или
     * 'Not Stable Message', означает, что изменен внутренний статус стабильности узла.
     * Таким образом, контроллер получает информацию и обновляет соответствующий статус
     * узла на карте. Если все узлы, присутствующие на карте, стабильны, вычисляется время,
     * прошедшее между этим событием и временем начала конвергенции.
     *
     * @param nodeId of the node that has sent this corresponding message
     * @param stable the value of stability carried with the message
     */
    synchronized void updateStable(Long nodeId, boolean stable) {
        nodeMap.get(nodeId).setStability(stable);
        if (stable) {
            int iter = 0;
            List<NodeInfo> tempList = new ArrayList<>(nodeMap.values());
            boolean tempStability = tempList.get(0).getStability();
            do {
                tempStability = tempStability && tempList.get(iter).getStability();
                iter++;
            } while (tempStability && (iter < tempList.size()));
            if (tempStability) {
                LocalTime endTimeStability = LocalTime.now();
                long elapsed = Duration.between(startTimeStability, endTimeStability).toMillis();
                double pass = elapsed / 1000.0;
                out.println(ANSI_GREEN + "Stabilization time : " + pass + " sec. - Nodes Number "
                        + nodeMap.size() + ANSI_RESET);
            }
        }
    }

    /**
     * Setting of the starting time of the lookup function.
     *
     * @param nodeId of the node that has sent this corresponding message
     */
    synchronized void startLookup(Long nodeId) {
        nodeMap.get(nodeId).setStartTimeLookup(LocalTime.now());
    }

    /**
     * Вычисляется время, прошедшее с момента начала операции до получения подтверждающего
     * сообщения из этой функциональности поступает информация о конкретном узле.
     *
     * @param nodeId of the node that has sent this corresponding message
     */
    synchronized void endLookup(Long nodeId) {
        LocalTime endTime = LocalTime.now();
        LocalTime startTime = nodeMap.get(nodeId).getStartTimeLookup();
        long elapsed = Duration.between(startTime, endTime).toMillis();
        double pass = elapsed / 1000.0;
        out.println(ANSI_CYAN + "Search time : " + pass + " sec. - Nodes number "
                + nodeMap.size() + ANSI_RESET);
    }

    /**
     * Setting of the starting time of the lookup function.
     *
     * @param nodeId of the node that has sent this corresponding message
     */
    synchronized void startInsertKey(Long nodeId) {
        nodeMap.get(nodeId).setStartTimeInsertKey(LocalTime.now());
    }

    /**
     * Is computed the elapsed time between the starting time of the operation, until the confirmation message
     * of that functionality is arrived about a specific node.
     *
     * @param nodeId of the node that has sent this corresponding message
     */
    synchronized void endInsertKey(Long nodeId) {
        LocalTime endTime = LocalTime.now();
        LocalTime startTime = nodeMap.get(nodeId).getStartTimeInsertKey();
        long elapsed = Duration.between(startTime, endTime).toMillis();
        double pass = elapsed / 1000.0;
        out.println(ANSI_PURPLE + "Tempo per Insert Key : " + pass + " sec. - Numero Nodi "
                + nodeMap.size() + ANSI_RESET);
    }

    /**
     * Setting of the starting time of the lookup function.
     *
     * @param nodeId of the node that has sent this corresponding message
     */
    synchronized void startFindKey(Long nodeId) {
        nodeMap.get(nodeId).setStartTimeFindKey(LocalTime.now());
    }

    /**
     * Вычисляется время, прошедшее между временем начала операции,
     * пока не поступит сообщение о подтверждении этой функциональности для конкретного узла.
     *
     * @param nodeId of the node that has sent this corresponding message
     */
    synchronized void endFindKey(Long nodeId) {
        LocalTime endTime = LocalTime.now();
        LocalTime startTime = nodeMap.get(nodeId).getStartTimeFindKey();
        long elapsed = Duration.between(startTime, endTime).toMillis();
        double pass = elapsed / 1000.0;
        out.println(ANSI_YELLOW + "Tempo per Find Key : " + pass + " sec. - Numero Nodi "
                + nodeMap.size() + ANSI_RESET);
    }
}
