package com.ssau.chord.model.network;



import com.ssau.chord.model.exceptions.ConnectionErrorException;
import com.ssau.chord.model.exceptions.TimerExpiredException;
import com.ssau.chord.model.exceptions.UnexpectedBehaviourException;
import com.ssau.chord.model.network.message.MessageHandler;
import com.ssau.chord.model.node.Hash;
import com.ssau.chord.model.node.Node;
import com.ssau.chord.model.node.NodeInterface;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.exit;

/**
 * Handle the socket connections of the node
 * node is the owner of the SocketManager
 * socketList карта между nodeId и соответствующими исходящими соединениями сокета
 * socketNumber карта между nodeId и числом от крытых коннектов с этой нодой
 */
public class SocketManager {
    private volatile Node node;
    private volatile Map<Long, NodeInterface> socketList;
    private volatile Map<Long, Integer> socketNumber;

    /**
     * Initialize class variables
     *
     * @param node node that creates the SocketManager
     */
    public SocketManager(Node node) {
        this.node = node;
        this.socketList = new HashMap<>();
        this.socketNumber = new HashMap<>();
    }

    /**
     * Used to create the outgoing socket connections
     *
     * @param connectionNode node to which you want to open the connection
     * @return if the nodeId of connectionNode is equal to that of node, return node
     * if there is already a open connection to the connectionNode, return that one increasing the
     * corresponding socketNumber, otherwise creates a new connection, add it to socketNumber, and return it
     * @throws ConnectionErrorException if the connectionNode is not reachable (wrong ipAddress or port)
     */
    public synchronized NodeInterface createConnection(NodeInterface connectionNode) throws ConnectionErrorException {
        Long searchedNodeId = connectionNode.getNodeId();
        if (searchedNodeId.equals(node.getNodeId())) { //nel caso in cui ritorno me stesso non ho bisogno di aggiornare il numero di connessioni
            //out.println("NUOVA CONNESSIONE: RITORNO ME STESSO");
            return node;
        } else {
            NodeInterface searchedNode = socketList.get(searchedNodeId);
            if (searchedNode != null) {
                //out.println("DALLA LISTA: " + searchedNodeId + ", NUM CONNECTION: " + (socketNumber.get(searchedNodeId)+1));
                int n = socketNumber.get(searchedNodeId); //vecchio numero di connessioni
                socketNumber.replace(searchedNodeId, n + 1); //faccio replace con nodeId e n+1
                return searchedNode;
            } else {
                //out.println("NUOVA: " + searchedNodeId);
                NodeCommunicator createdNode = new NodeCommunicator(connectionNode.getIpAddress(), connectionNode.getSocketPort(), node, connectionNode.getNodeId());
                socketList.put(searchedNodeId, createdNode);
                socketNumber.put(searchedNodeId, 1); //quando creo un nodo inserisco nella lista <nodeId, 1>
                return createdNode;
            }
        }
    }

    /**
     * Используется для создания входящих подключений к сокетам.
     * Метод, вызываемый только конструктором узла сокета
     *
     * @param socketNode socketNode of node that is connecting
     * @param ipAddress  ipAddress of node that is connecting
     */
    synchronized void createConnection(SocketNode socketNode, String ipAddress) {
        NodeInterface createdNode = new NodeCommunicator(socketNode, node, ipAddress);
        socketNode.setMessageHandler((MessageHandler) createdNode);
        int port;
        try {
            port = createdNode.getInitialSocketPort();
        } catch (TimerExpiredException e) {
            err.println("Error of connection");
            exit(1);
            throw new UnexpectedBehaviourException();
        }
        Long createdNodeId = Hash.getHash().calculateHash(ipAddress, port);
        if (!createdNodeId.equals(node.getNodeId())) {
            createdNode.setNodeId(createdNodeId);
            createdNode.setSocketPort(port);
        } else {
            try {
                createdNode.close();
            } catch (IOException e) {
                throw new UnexpectedBehaviourException();
            }
        }
        //out.println("CREO: " + createdNode.getNodeId());
    }

    /**
     * Если NodeID имеет число открытых подключений большее или равное 2:
     * просто уменьшите на единицу количество подключений
     *
     * Если NodeID имеет только 1 открытое соединение:
     * закройте соединение и удалите его из списка сокетов и номера сокета
     *
     * @param nodeId nodeId of node to which we want close the connection
     */
    public synchronized void closeCommunicator(Long nodeId) {
        //eseguo solo se il nodeId da rimuovere non è il mio
        if (!node.getNodeId().equals(nodeId)) { //non posso rimuovere me stesso dalla lista
            Integer n = socketNumber.get(nodeId); //old connection number
            if (n != null) {
                if (n == 1) { //removes the connection
                    socketNumber.remove(nodeId);
                    socketList.remove(nodeId);
                    //out.println("RIMOSSO: " + nodeId);
                } else { //decreases the number of connection
                    socketNumber.replace(nodeId, n - 1);
                    //out.println("DIMINUISCO: " + nodeId + ", NUM CONNECTION: " + socketNumber.get(nodeId));
                }
            }
        }
    }

    /**
     * Called only when the connection to the disconnectedId is dropped
     *
     * @param disconnectedId nodeId of disconnected node
     */
    synchronized void removeNode(Long disconnectedId) {
        node.checkDisconnectedNode(disconnectedId);
        socketList.remove(disconnectedId);
        socketNumber.remove(disconnectedId);
    }

    @Override
    public String toString() {
        String string = "SOCKET OPEN\n";
        for (Map.Entry it :
                socketList.entrySet()) {
            string = string + "Node id: " + it.getKey() + "\tNumber conn: " + socketNumber.get(it.getKey()) + "\n";
        }
        return string;
    }
}