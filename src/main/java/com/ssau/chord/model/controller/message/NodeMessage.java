package com.ssau.chord.model.controller.message;

import java.io.IOException;
import java.io.Serializable;

/**
 * Интерфейс описывает сообщения от Контроллера к Node
 */
public interface NodeMessage extends Serializable {
    /**
     * Handles the message
     *
     * @param nodeMessageHandler responsible of the handling
     * @throws IOException if an I/O error occurs
     */
    void handle(NodeMessageHandler nodeMessageHandler) throws IOException;
}
