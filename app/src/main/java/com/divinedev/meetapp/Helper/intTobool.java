package com.divinedev.meetapp.Helper;

public class intTobool {
    public intTobool() {

    }
    public boolean intOf(int val){
        if (val==0){
            return false;
        }
        else
            return true;
    }
    public int boolOf(boolean val){
        if (val)
            return 1;
        else return 0;
    }
}
