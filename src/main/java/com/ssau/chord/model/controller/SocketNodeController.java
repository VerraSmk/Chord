package com.ssau.chord.model.controller;



import com.ssau.chord.model.controller.message.ControllerMessage;
import com.ssau.chord.model.controller.message.NodeMessage;
import com.ssau.chord.model.exceptions.ConnectionErrorException;
import com.ssau.chord.model.exceptions.UnexpectedBehaviourException;
import com.ssau.chord.model.node.Node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.Executors;

import static java.lang.System.err;

/**
 * Узел сокета, которая занимается отправкой и приемом сообщений к/от контроллера
 */
public class SocketNodeController implements Runnable, Serializable {
    private String ipAddress;
    private int socketPort;
    private transient Socket socketController;
    private transient ObjectOutputStream out;
    private transient ObjectInputStream in;
    private transient NodeControllerCommunicator controller;
    private volatile boolean connected;

    /**
     * @param ipAddress  ipAddress of Controller
     * @param socketPort socketPort on which Controller is reachable
     */
    public SocketNodeController(String ipAddress, int socketPort) {
        this.ipAddress = ipAddress;
        this.socketPort = socketPort;
    }

    /**
     * Creates the output/input stream to the Controller, and also creates the controller
     * responsible to handle the received message
     *
     * @param node node that creates the connection
     * @return the created controller
     * @throws ConnectionErrorException if statistics is not reachable at (ipAddress, socketPort)
     * @throws IOException              if an I/O error occurs
     */
    public NodeControllerCommunicator openController(Node node) throws ConnectionErrorException, IOException {
        this.connected = true;
        try {
            socketController = new Socket(ipAddress, socketPort);
        } catch (IOException e) {
            throw new ConnectionErrorException();
        }
        this.out = new ObjectOutputStream(socketController.getOutputStream());
        this.in = new ObjectInputStream(socketController.getInputStream());
        this.controller = new NodeControllerCommunicator(node, this);
        Executors.newCachedThreadPool().submit(this);
        return controller;
    }

    /**
     * Пока соединение открыто (connected == true), метод вызывает GetMessage для получения
     * сообщения от контроллера
     */
    @Override
    public void run() {
        while (connected) {
            NodeMessage message = getMessage();
            if (!connected)
                break;
            Executors.newCachedThreadPool().execute(() -> {
                try {
                    message.handle(controller);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Получает сообщение от контроллера, и когда контроллер был отключен,
     * вызывает метод disconnected Controller коммуникатора контроллера узла
     *
     * @return the received message
     */
    private NodeMessage getMessage() {
        try {
            return (NodeMessage) in.readObject();
        } catch (IOException e) {
            controller.disconnectedController();
            err.println("Controller Disconnected");
            connected = false;
            this.close();
        } catch (ClassNotFoundException e) {
            throw new UnexpectedBehaviourException();
        }
        return null;
    }

    /**
     * Sends the ControllerMessage to Controller
     *
     * @param message message to send
     * @throws IOException if an I/O error occurs
     */
    synchronized void sendMessage(ControllerMessage message) throws IOException {
        out.reset();
        out.writeObject(message);
        out.flush();
    }

    /**
     * Close the socketInputStream and the socketOutputStream
     */
    private void close() {
        try {
            in.close();
            out.close();
            socketController.close();
            this.connected = false;
        } catch (IOException e) {
            throw new UnexpectedBehaviourException();
        }
    }
}