/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physics06;

/**
 *
 * @author Administrator
 */
public class Segment2f {
    public float x1, y1, x2, y2, timeIn, timeOut;

    public Segment2f() {
    }

    public Segment2f(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    public Segment2f(Vector2f a, Vector2f b, float timeIn, float timeOut) {
        x1 = a.x;
        y1 = a.y;
        x2 = b.x;
        y2 = b.y;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }
}
