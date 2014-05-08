/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physics06;

/**
 *
 * @author Administrator
 */
public class Vector2f {
    public float x = 0, y = 0;

    public Vector2f() {
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + Physics06VisState.format(x, 2) + ", " + 
                Physics06VisState.format(y, 2) + ")";
    }
    
    public float getLength() {
        return (float)Math.sqrt(x * x + y * y);
    }
    
    @Override
    public Vector2f clone() {
        return new Vector2f(x, y);
    }
    
    public Vector2f add(Vector2f a) {
        return new Vector2f(x + a.x, y + a.y);
    }
    
}
