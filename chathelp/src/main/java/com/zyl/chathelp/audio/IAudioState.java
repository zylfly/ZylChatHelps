package com.zyl.chathelp.audio;

public abstract class IAudioState {
    public IAudioState() {
    }

    void enter() {
    }

    abstract void handleMessage(AudioStateMessage var1);
}