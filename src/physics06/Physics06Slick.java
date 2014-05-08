/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physics06;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author Administrator
 */
public class Physics06Slick extends StateBasedGame {

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
    }

    public Physics06Slick(String name) throws SlickException {
        super(name);
        addState(new Physics06VisState());
        enterState(0);
    }
    
    
    
}
