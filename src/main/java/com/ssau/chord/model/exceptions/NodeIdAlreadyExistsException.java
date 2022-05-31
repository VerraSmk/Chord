package com.ssau.chord.model.exceptions;

public class NodeIdAlreadyExistsException extends Exception {
    public NodeIdAlreadyExistsException() {
        super();
    }

    public NodeIdAlreadyExistsException(String s) {
        super(s);
    }
}