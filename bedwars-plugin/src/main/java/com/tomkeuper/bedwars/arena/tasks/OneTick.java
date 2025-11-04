package com.tomkeuper.bedwars.arena.tasks;

import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.arena.OreGenerator;

public class OneTick implements Runnable {
    @Override
    public void run() {
        // OneTick generators
        for (IGenerator h : OreGenerator.getRotation()) {
            h.rotate();
        }
    }
}
