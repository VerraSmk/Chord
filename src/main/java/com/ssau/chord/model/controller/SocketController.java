package com.ssau.chord.model.controller;


import com.ssau.chord.model.controller.message.ControllerMessage;
import com.ssau.chord.model.controller.message.NodeMessage;
import com.ssau.chord.model.exceptions.UnexpectedBehaviourException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Socket Controller-side that deals the sending and receiving of Message to/from the node
 */
public class SocketController implements Runnable, Serializable {
    private transient ControllerCommunicator controller;
    private transient ObjectInputStream socketInput;
    private transient ObjectOutputStream socketOutput;
    private transient volatile boolean connected = true;

    /**
     * @param controller Controller singleton
     * @param nodeSocket socket of incoming connection, from which we create the input and output stream
     */
    public SocketController(Controller controller, Socket nodeSocket) {
        this.controller = new ControllerCommunicator(controller, this);
        try {
            this.socketInput = new ObjectInputStream(nodeSocket.getInputStream());
            this.socketOutput = new ObjectOutputStream(nodeSocket.getOutputStream());
        } catch (IOException e) {
            this.close();
        }
    }

    /**
     * Пока нода подключена (connected == true) метод вызывает getMessage метод
     * для получения сообщения от узла
     */
    @Override
    public void run() {
        while (connected) {
            ControllerMessage message = getMessage();
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
     * Получает сообщение от узла, и когда другой узел отключился,
     * вызывает метод отключения узла коммуникатора контроллера
     *
     * @return the received message
     */
    private ControllerMessage getMessage() {
        try {
            return (ControllerMessage) socketInput.readObject();
        } catch (IOException e) {
            connected = false;
            this.close();
            controller.disconnectedNode();
        } catch (ClassNotFoundException e) {
            throw new UnexpectedBehaviourException();
        }
        return null;
    }

    /**
     * Sends the NodeMessage to the node
     *
     * @param message message to send
     * @throws IOException if an I/O error occurs
     */
    synchronized void sendMessage(NodeMessage message) throws IOException {
        socketOutput.reset();
        socketOutput.writeObject(message);
        socketOutput.flush();
    }

    /**
     * Close the socketInputStream and the socketOutputStream
     */
    private void close() {
        try {
            socketInput.close();
            socketOutput.close();
            this.connected = false;
        } catch (IOException e) {
            throw new UnexpectedBehaviourException();
        }
    }
}
