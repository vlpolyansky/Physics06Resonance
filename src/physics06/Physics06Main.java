package physics06;

import java.awt.Color;
import java.io.PrintStream;
import java.util.Locale;
import javax.swing.UIManager;

public class Physics06Main {

    public static void main(String[] args) {
        System.setOut(new PrintStream(Physics06Settings.consoleOut));
        Locale.setDefault(Locale.UK);
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("nimbusOrange", Color.blue);
        } catch (Exception ex) {
            Physics06Settings.error(ex);
        }
        new Physics06Frame();
    }
}
