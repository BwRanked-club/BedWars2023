package com.tomkeuper.bedwars.arena.tasks;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.entity.Despawnable;

public class Refresh implements Runnable {

    @Override
    public void run() {
        for (Despawnable d : BedWars.nms.getDespawnablesList().values()){
            d.refresh();
        }
    }
}
