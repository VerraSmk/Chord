package com.ssau.chord.model.controller.message;

import java.io.IOException;

/**
 * Интерфейс, реализованный Node Controller , ответственным за обработку
 * сообщений от контроллера
 */
public interface NodeMessageHandler {
    /**
     * @param receivedMessage the received ReceivedMessage
     * @throws IOException if an I/O error occurs
     */
    void handle(ReceivedMessage receivedMessage) throws IOException;
}