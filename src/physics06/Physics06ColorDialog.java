package physics06;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Physics06ColorDialog extends JDialog {

    public Physics06ColorDialog(JFrame owner) {
        super(owner, true);
        init();
        setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);
        setVisible(true);
    }

    private java.awt.Color another(org.newdawn.slick.Color c) {
        return new java.awt.Color(c.r, c.g, c.b, c.a);
    }

    private org.newdawn.slick.Color another(java.awt.Color c) {
        return new org.newdawn.slick.Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    private void init() {
        Deque<JButton> buttons = new ArrayDeque<JButton>();
//        Deque<JPanel> panels = new ArrayDeque<JPanel>();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        JButton tmp = null;
        for (final Field field : Physics06Settings.class.getFields()) {
            if (!field.getType().getSimpleName().equals("Color")) {
                continue;
            }
            tmp = new JButton(field.getName());
            try {
                tmp.setForeground(another((org.newdawn.slick.Color) field.get(Physics06Settings.class)));
            } catch (Exception e) {
                Physics06Settings.log(null, e);
            }
//            JPanel pan = new JPanel();
//            try {
//                pan.setBackground(another(
//                        (org.newdawn.slick.Color)field.get(Physics06Settings.class)));
//            } catch (Exception e) {
//                Physics06Settings.log(null, e);
//            } 
//            GroupLayout layout = new GroupLayout(pan);
//            pan.setLayout(layout);
//            layout.setHorizontalGroup(
//                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 23, Short.MAX_VALUE));
//            layout.setVerticalGroup(
//                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 23, Short.MAX_VALUE));
            tmp.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        org.newdawn.slick.Color slickWas =
                                (org.newdawn.slick.Color) field.get(Physics06Settings.class);
                        java.awt.Color was = new java.awt.Color(slickWas.r, slickWas.g,
                                slickWas.b, slickWas.a);
                        java.awt.Color cur = JColorChooser.showDialog(getThis(), field.getName(), was);
                        if (cur != null) {
                            org.newdawn.slick.Color slickCur = new org.newdawn.slick.Color(
                                    cur.getRed(), cur.getGreen(), cur.getBlue(), slickWas.getAlpha());
                            field.set(Physics06Settings.class, slickCur);
                        }
                        ((JButton)e.getSource()).setForeground(cur);
                    } catch (Exception exc) {
                        Physics06Settings.log("Exception while changing color", exc);
                    }
                }
            });
            buttons.addFirst(tmp);
//            panels.addFirst(pan);
        }
        tmp = new JButton("OK");
        tmp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttons.addFirst(tmp);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        ParallelGroup parGr = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING);
        Deque<JButton> buttons2 = new ArrayDeque<JButton>();
        Deque<JPanel> panels2 = new ArrayDeque<JPanel>();
        for (JButton button : buttons) {
            parGr = parGr.addComponent(button, javax.swing.GroupLayout.Alignment.LEADING,
                    javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE);
            buttons2.addFirst(button);
        }
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(parGr).addContainerGap()));
        SequentialGroup seqGr = layout.createSequentialGroup().addContainerGap();
        for (JButton button : buttons2) {
            seqGr = seqGr.addComponent(button).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        }
        seqGr = seqGr.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(seqGr));

        pack();
    }

    private void refresh() {
        for (Component c : this.getComponents()) {
            if (c instanceof JButton) {
                try {
                    if (!((JButton) c).getText().equals("OK")) {
                        c.setForeground(another((org.newdawn.slick.Color) Physics06Settings.class.getField(((JButton) c).getText()).get(Physics06Settings.class)));
                    }
                } catch (Exception e) {
                    Physics06Settings.log(null, e);
                }
            }
        }
    }

    private Physics06ColorDialog getThis() {
        return this;
    }
}
