package com.ssau.chord.model.controller;

import java.time.LocalTime;

/**
 Целью этого класса является отслеживание статистики каждого подключенного узла
 к контроллеру. Этот объект используется в карте контроллера в качестве значения,
 связанного с картой, где ключом является NodeID.
 Контроллер:
 - Стабильность работы Сети после нового подключения или отключения
 - Время запуска методод Lookup, InsertKey, FindKey
 */
class NodeInfo {
    private boolean stable;
    private LocalTime startTimeLookup;
    private LocalTime startTimeInsertKey;
    private LocalTime startTimeFindKey;

    NodeInfo() {
        this.stable = false;
    }

    boolean getStability() {
        return this.stable;
    }

    void setStability(boolean stability) {
        this.stable = stability;
    }

    LocalTime getStartTimeLookup() {
        return this.startTimeLookup;
    }

    void setStartTimeLookup(LocalTime startTimeLookup) {
        this.startTimeLookup = startTimeLookup;
    }

    LocalTime getStartTimeInsertKey() {
        return startTimeInsertKey;
    }

    void setStartTimeInsertKey(LocalTime startTimeInsertKey) {
        this.startTimeInsertKey = startTimeInsertKey;
    }

    LocalTime getStartTimeFindKey() {
        return startTimeFindKey;
    }

    void setStartTimeFindKey(LocalTime startTimeFindKey) {
        this.startTimeFindKey = startTimeFindKey;
    }
}