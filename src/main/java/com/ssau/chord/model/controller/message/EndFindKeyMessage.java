package com.ssau.chord.model.controller.message;

import java.io.IOException;

public class EndFindKeyMessage implements ControllerMessage {
    private Long lockId;

    public EndFindKeyMessage(Long lockId) {
        this.lockId = lockId;
    }

    @Override
    public void handle(ControllerMessageHandler controllerMessageHandler) throws IOException {
        controllerMessageHandler.handle(this);
    }

    public Long getLockId() {
        return lockId;
    }
}