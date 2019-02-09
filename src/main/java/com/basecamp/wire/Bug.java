package com.basecamp.wire;

import lombok.Value;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

@Value
public class Bug {
    private int timemoment;
    private String nameofBug;
    private int step;

    public Bug(int timemoment, String nameofBug, int step) {
        this.timemoment = timemoment;
        this.nameofBug = nameofBug;
        this.step = step;
    }

    public int getTimemoment() {
        return timemoment;
    }

    @Override
    public String toString() {
        return "Bug named '" + nameofBug +
                "', reached FINISH at " +
                timemoment + " timemoment, on step  " + step + ".";
    }

}
