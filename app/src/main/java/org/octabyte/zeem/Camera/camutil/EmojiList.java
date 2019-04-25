package org.octabyte.zeem.Camera.camutil;

/**
 * Created by Azeem on 6/24/2017.
 */

class EmojiList {
    private int x,y;
    private String src, uid;

    public EmojiList(int x, int y, String src, String uid) {
        this.x = x;
        this.y = y;
        this.src = src;
        this.uid = uid;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public String getSrc() {
        return src;
    }
    public String getUid() {
        return uid;
    }
}