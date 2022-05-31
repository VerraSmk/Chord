package com.ssau.chord.model.controller;

import com.ssau.chord.model.controller.message.*;
import com.ssau.chord.model.node.Node;

import java.io.IOException;
import java.util.HashMap;

/**
 Этот класс доступен со стороны узла. Каждый узел имеет открытый сокет для контроллера.
 Каждый раз, когда выполняется операция, проверяемая контроллером, запускается соответствующий
 метод. Каждый метод создает правильное сообщение и отправляет его контроллеру через сокет.
 Часто одна операция состоит из двух частей с использованием двух разных методов.
 Таким образом, мы можем определить начало операции и ее завершение. Класс также обрабатывает
 подтверждающие сообщения, поступающие от контроллера, таким образом, мы можем уведомлять
 методы ожидания о ресурсах.
 */
public class NodeControllerCommunicator implements NodeMessageHandler, ControllerInterface {
    private Node node;
    private SocketNodeController controller;
    private volatile HashMap<Long, Object> lockList = new HashMap<>();
    private volatile Long lockID = 0L;

    NodeControllerCommunicator(Node node, SocketNodeController controller) {
        this.node = node;
        this.controller = controller;
    }

    /**
     * Creates the lock object
     *
     * @return lockId corresponding to the created lock object
     */
    private synchronized Long createLock() {
        lockList.put(lockID, new Object());
        lockID = lockID + 1;
        return lockID - 1;
    }

    @Override
    public void connected() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new ConnectedMessage(node.getNodeId(), lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void stable() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new StableMessage(lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void notStable() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new NotStableMessage(lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void startLookup() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new StartLookupMessage(lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void endOfLookup() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new EndOfLookupMessage(lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void startInsertKey() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new StartInsertKeyMessage(lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void endInsertKey() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new EndInsertKeyMessage(lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void startFindKey() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new StartFindKeyMessage(lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void endFindKey() throws IOException {
        Long lockId = createLock();
        synchronized (lockList.get(lockId)) {
            controller.sendMessage(new EndFindKeyMessage(lockId));
            try {
                lockList.get(lockId).wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void disconnectedController() {
        node.disconnectedController();
    }

    @Override
    public void handle(ReceivedMessage receivedMessage) throws IOException {
        synchronized (lockList.get(receivedMessage.getLockId())) {
            lockList.get(receivedMessage.getLockId()).notifyAll();
        }
    }
}