package com.amila.dev.virtualmenu;

public class Counter {
    private int c;

    public Counter() {
        this.c = 0;
    }

    public void increment(){
        c++;
    }

    public int getVal(){
        return c;
    }

    public void reset(){
        c = 0;
    }
}
