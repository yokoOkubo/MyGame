package yoko.puyo.mygame;
//SurfaceViewに置くもののsuper class
public class GameObject{
    private int left;
    private int top;
    private int width;
    private int height;

    public GameObject(int left, int top, int width, int height) {
        setLocate(left, top);
        this.width = width;
        this.height = height;
    }
    public void setLocate(int left, int top) {
        this.left = left;
        this.top = top;
    }
    public void move(int left, int top) {
        this.left += left;
        this.top += top;
    }
    public int getLeft() {
        return left;
    }
    public int getRight() {
        return left+width;
    }
    public int getTop() {
        return top;
    }
    public int getBottom() {
        return top + height;
    }
    public int centerX() {
        return left + width/2;
    }
    public int centerY() {
        return top + height/2;
    }
}
