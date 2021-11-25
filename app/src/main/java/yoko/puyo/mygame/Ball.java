package yoko.puyo.mygame;

import android.graphics.Region;

import java.util.List;

public class Ball extends GameObject{
    public Ball(int left, int top, int width, int height) {
        super(left, top, width, height);
    }
    //--------------------------------------------------障害物にぶつかったか
    public boolean isColligion(List<Region> regionList ) {
        for(Region r : regionList) {
            if(!r.quickReject(getLeft(),getTop(),getRight(),getBottom())) return true; //ぶつかった
        }
        return false;
    }
    //--------------------------------------------------ゴールしたか
    public boolean isGoal(Region goalZone) {
        if (goalZone.quickContains(getLeft(),getTop(),getRight(),getBottom())) {
            return true;
        }
        return false;
    }
}
