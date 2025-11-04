package com.tomkeuper.bedwars.api.exceptions;

import com.tomkeuper.bedwars.api.server.VersionSupport;

public class InvalidSoundException extends Throwable {


    public InvalidSoundException(String s) {
        super(s + " is not a valid " + VersionSupport.getName() + " sound! Using defaults..");
    }
}
