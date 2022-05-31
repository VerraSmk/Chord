package com.ssau.chord.model.controller;



import com.ssau.chord.model.controller.message.*;

import java.io.IOException;

/**
 * Коммуникатор контроллера находится на стороне контроллера и выполняет
 * обработку входящих сообщений и вызовов соответствующий метод контроллера.
 */
public class ControllerCommunicator implements ControllerMessageHandler {
    private Controller controller;
    private SocketController socketController;
    private Long nodeId;

    ControllerCommunicator(Controller controller, SocketController socketController) {
        this.controller = controller;
        this.socketController = socketController;
    }

    @Override
    public void handle(ConnectedMessage connectedMessage) throws IOException {
        this.nodeId = connectedMessage.getNodeId();
        controller.newConnection(connectedMessage.getNodeId());
        socketController.sendMessage(new ReceivedMessage(connectedMessage.getLockId()));
    }

    @Override
    public void handle(StableMessage stableMessage) throws IOException {
        controller.updateStable(nodeId, true);
        socketController.sendMessage(new ReceivedMessage(stableMessage.getLockId()));
    }

    @Override
    public void handle(NotStableMessage notstableMessage) throws IOException {
        controller.updateStable(nodeId, false);
        socketController.sendMessage(new ReceivedMessage(notstableMessage.getLockId()));
    }

    @Override
    public void handle(StartLookupMessage startLookupMessage) throws IOException {
        controller.startLookup(nodeId);
        socketController.sendMessage(new ReceivedMessage(startLookupMessage.getLockId()));
    }

    @Override
    public void handle(EndOfLookupMessage endOfLookupMessage) throws IOException {
        controller.endLookup(nodeId);
        socketController.sendMessage(new ReceivedMessage(endOfLookupMessage.getLockId()));
    }

    @Override
    public void handle(StartInsertKeyMessage startInsertKeyMessage) throws IOException {
        controller.startInsertKey(nodeId);
        socketController.sendMessage(new ReceivedMessage(startInsertKeyMessage.getLockId()));
    }

    @Override
    public void handle(EndInsertKeyMessage endInsertKeyMessage) throws IOException {
        controller.endInsertKey(nodeId);
        socketController.sendMessage(new ReceivedMessage(endInsertKeyMessage.getLockId()));
    }

    @Override
    public void handle(StartFindKeyMessage startFindKeyMessage) throws IOException {
        controller.startFindKey(nodeId);
        socketController.sendMessage(new ReceivedMessage(startFindKeyMessage.getLockId()));
    }

    @Override
    public void handle(EndFindKeyMessage endFindKeyMessage) throws IOException {
        controller.endFindKey(nodeId);
        socketController.sendMessage(new ReceivedMessage(endFindKeyMessage.getLockId()));
    }

    void disconnectedNode() {
        controller.disconnectedNode(nodeId);
    }
}
