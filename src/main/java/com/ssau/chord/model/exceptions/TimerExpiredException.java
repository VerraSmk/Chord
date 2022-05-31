package com.ssau.chord.model.exceptions;

public class TimerExpiredException extends Exception {
    public TimerExpiredException() {
        super();
    }

    public TimerExpiredException(String s) {
        super(s);
    }
}
