package com.ssau.chord.model.controller.message;

import java.io.IOException;
import java.io.Serializable;

/**
 * Интерфейс, для сообщений, которые передаются от узла к контроллеру
 */
public interface ControllerMessage extends Serializable {

    /**
     * Handles the message
     *
     * @param controllerMessageHandler responsible of the handling
     * @throws IOException if an I/O error occurs
     */
    void handle(ControllerMessageHandler controllerMessageHandler) throws IOException;
}
