package com.ssau.chord.model.controller;

/**
 * Класс, используемый для замены реального коммуникатора контроллера узла,
 * имеет тот же метод, но пустой, чтобы обеспечить правильное поведение узла,
 * даже если контроллер был отключен
 *
 */
public class DisconnectedController implements ControllerInterface {
    @Override
    public void connected() {
        //do nothing
    }

    @Override
    public void stable() {
        //do nothing
    }

    @Override
    public void notStable() {
        //do nothing
    }

    @Override
    public void startLookup() {
        //do nothing
    }

    @Override
    public void endOfLookup() {
        //do nothing
    }

    @Override
    public void startInsertKey() {
        //do nothing
    }

    @Override
    public void endInsertKey() {
        //do nothing
    }

    @Override
    public void startFindKey() {
        //do nothing
    }

    @Override
    public void endFindKey() {
        //do nothing
    }
}
