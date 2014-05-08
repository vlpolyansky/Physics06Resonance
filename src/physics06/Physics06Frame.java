/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Physics02Frame.java
 *
 * Created on 29.09.2011, 17:18:46
 */
package physics06;

import java.io.IOException;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.newdawn.slick.CanvasGameContainer;
import org.newdawn.slick.SlickException;
import static physics06.Physics06Settings.*;

/**
 *
 * @author Administrator
 */
public class Physics06Frame extends javax.swing.JFrame {

    private static int windowWidth = 800, windowHeight = 600;
    private CanvasGameContainer visCanvas = null;
    private Color backColor = Color.black;
    private static long[] power10;

    static {
        power10 = new long[15];
        power10[0] = 1;
        for (int i = 1; i < power10.length; i++) {
            power10[i] = 10 * power10[i - 1];
        }
    }

    /** Creates new form Physics02Frame */
    public Physics06Frame() {
        initComponents();
        myInit();
        jSliderdX2StateChanged(null);
        jSliderdY2StateChanged(null);
        jSliderdY1StateChanged(null);
    }

    private void myInit() {
        try {
            frame = this;
            //-----------------------------------------------
            final Physics06Slick sl = new Physics06Slick("Physics");
            visCanvas = new CanvasGameContainer(sl);
            visCanvas.setSize(1600, 1200);
            visCanvas.setVisible(true);
            slickPanel.add(visCanvas);
            setSize(windowWidth, windowHeight);
            setLocationRelativeTo(null);
            lastUpdate = -1;
            //------------------------------------------------
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent e) {
                    System.exit(0);
                }
            });
            addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    super.componentResized(e);
                    resized();
                }
            });
            //------------------------------------------------
            setSize(850, 600);
            //------------------------------------------------
            addKeyListener(this, new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    formKeyPressed(e);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
            //------------------------------------------------
            width = slickPanel.getWidth();
            height = slickPanel.getHeight() - 2 * graphSeparation;
            jProgressBar1.setMaximum(maxPeriodsNeeded + 1);
            pack();
            setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2,
                    (Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);
            visCanvas.start();
            setVisible(true);
        } catch (SlickException ex) {
            error(ex);
            return;
        }

    }

    private void addKeyListener(Component c, KeyListener kl) {
        c.addKeyListener(kl);
        if (c instanceof JFrame) {
            for (Component comp : ((JFrame) c).getComponents()) {
                addKeyListener(comp, kl);
            }
        } else if (c instanceof JPanel) {
            for (Component comp : ((JPanel) c).getComponents()) {
                addKeyListener(comp, kl);
            }
        } else if (c instanceof JComponent) {
            for (Component comp : ((JComponent) c).getComponents()) {
                addKeyListener(comp, kl);
            }
        }
    }

    public void resized() {
        int newH = slickPanel.getHeight() - 3 * graphSeparation;
        width = slickPanel.getWidth();
        height = newH;
        height1 = (height) * h1Part;
        height2 = height - height1;
        graph1dX = (chosenRightW - chosenLeftW) / (float) (width - 2 * graphSeparation);
        graph1dY = getMultiplier(jSliderdY1.getValue()) / (float) height1;
        graph2dY = getMultiplier(jSliderdY2.getValue()) / (float) height2;
    }

    public JPanel getSlickPanel() {
        return slickPanel;
    }

    public CanvasGameContainer getVisCanvas() {
        return visCanvas;
    }

    public void renew() {
        jButtonStart.setEnabled(true);
        jButtonSkip.setEnabled(searchType == SearchType.DELTA);
        jLabelB.setText("b=" + b);
        jLabelBeta.setText("β=" + beta);
        jLabelF.setText("F=" + F);
        jLabelK.setText("k=" + k);
        jLabelM.setText("m=" + m);
        jLabelOmega0.setText("ω0=" + w0);
        jLabelAV.setText(customDT == 0
                ? "Среднее dt=" + Physics06VisState.format(averageDT * 1000, 1) + "мс"
                : "dt=" + customDT + "мс");
        setW(w);
        if (autosize) {
            jSliderdY1.setValue(2);
            jSliderdY2.setValue(2);
        }
        for (Vector2f v : graphRes) {
            while (2 * v.y / graph1dY > height1) {
                jSliderdY1.setValue(jSliderdY1.getValue() + 1);
            }
        }
        for (Vector2f v : graphSpring) {
            while (v.y / graph2dY > height2) {
                jSliderdY2.setValue(jSliderdY2.getValue() + 1);
            }
        }
        resized();
    }

    public void setAV(float av) {
        jLabelAV.setText("Среднее dt=" + Physics06VisState.format(av * 1000, 1) + "мс");
    }

    public void setW(float w) {
        jLabelCurW.setText("Текущее ω=" + Physics06VisState.format(w, 4));
    }

    public void setDone(int d) {
        if (d > maxPeriodsNeeded) {
            d = maxPeriodsNeeded;
        }
        float part = d / (float) maxPeriodsNeeded;
        Color c;
        if (d < maxPeriodsNeeded / 2f) {
            c = new Color(1f, 2f * part, 0f);
        } else {
            c = new Color(2f - 2f * part, 1f, 0f);
        }
        jProgressBar1.setValue(d + 1);
        try {
            UIManager.put("nimbusOrange", c);
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            jProgressBar1.updateUI();
        } catch (Exception exc) {
            log("Exception while changing ProgressBar color", exc);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        slickPanel = new javax.swing.JPanel();
        settingPanel = new javax.swing.JPanel();
        jButtonStart = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabelAV = new javax.swing.JLabel();
        jSliderdX2 = new javax.swing.JSlider();
        jSliderdY2 = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jCheckBoxFollow = new javax.swing.JCheckBox();
        jLabel01 = new javax.swing.JLabel();
        jLabelBeta = new javax.swing.JLabel();
        jLabelOmega0 = new javax.swing.JLabel();
        jLabelF = new javax.swing.JLabel();
        jLabelK = new javax.swing.JLabel();
        jLabelM = new javax.swing.JLabel();
        jLabelB = new javax.swing.JLabel();
        jSliderdY1 = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jLabelCurW = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButtonNew = new javax.swing.JButton();
        jButtonOpen = new javax.swing.JButton();
        jCheckBoxAccent = new javax.swing.JCheckBox();
        jCheckBoxConnect = new javax.swing.JCheckBox();
        jButtonChangeColors = new javax.swing.JButton();
        jButtonSkip = new javax.swing.JButton();
        jButtonFuncGraph = new javax.swing.JButton();
        jCheckBoxAutosize = new javax.swing.JCheckBox();
        jButtonHelp = new javax.swing.JButton();
        jButtonConsole = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Физика: вынужденные колебания, построение резонансной кривой");
        setBackground(new java.awt.Color(0, 0, 0));
        setMinimumSize(new java.awt.Dimension(800, 600));

        slickPanel.setBackground(new java.awt.Color(51, 51, 51));
        slickPanel.setPreferredSize(new java.awt.Dimension(600, 600));

        javax.swing.GroupLayout slickPanelLayout = new javax.swing.GroupLayout(slickPanel);
        slickPanel.setLayout(slickPanelLayout);
        slickPanelLayout.setHorizontalGroup(
            slickPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1111, Short.MAX_VALUE)
        );
        slickPanelLayout.setVerticalGroup(
            slickPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 550, Short.MAX_VALUE)
        );

        settingPanel.setBackground(new java.awt.Color(0, 0, 0));

        jButtonStart.setText("Пуск/Пауза");
        jButtonStart.setEnabled(false);
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        jLabelAV.setForeground(new java.awt.Color(255, 255, 255));
        jLabelAV.setText("Среднее dt=0мс");

        jSliderdX2.setMaximum(8);
        jSliderdX2.setValue(4);
        jSliderdX2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderdX2StateChanged(evt);
            }
        });

        jSliderdY2.setMaximum(15);
        jSliderdY2.setValue(6);
        jSliderdY2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderdY2StateChanged(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Масштаб №2");

        jCheckBoxFollow.setBackground(new java.awt.Color(0, 0, 0));
        jCheckBoxFollow.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxFollow.setText("Следовать");
        jCheckBoxFollow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFollowActionPerformed(evt);
            }
        });

        jLabel01.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel01.setForeground(new java.awt.Color(255, 255, 255));
        jLabel01.setText("Параметры системы");

        jLabelBeta.setForeground(new java.awt.Color(255, 255, 255));
        jLabelBeta.setText("β=");

        jLabelOmega0.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOmega0.setText("ω0=");

        jLabelF.setForeground(new java.awt.Color(255, 255, 255));
        jLabelF.setText("F=");

        jLabelK.setForeground(new java.awt.Color(255, 255, 255));
        jLabelK.setText("k=");

        jLabelM.setForeground(new java.awt.Color(255, 255, 255));
        jLabelM.setText("m=");

        jLabelB.setForeground(new java.awt.Color(255, 255, 255));
        jLabelB.setText("b=");

        jSliderdY1.setMaximum(15);
        jSliderdY1.setValue(6);
        jSliderdY1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderdY1StateChanged(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Масштаб №1");

        jLabelCurW.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabelCurW.setForeground(new java.awt.Color(255, 0, 51));
        jLabelCurW.setText("ω=");

        jProgressBar1.setMaximum(4);

        jButtonNew.setText("Новый");
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });

        jButtonOpen.setText("Открыть");
        jButtonOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenActionPerformed(evt);
            }
        });

        jCheckBoxAccent.setBackground(new java.awt.Color(0, 0, 0));
        jCheckBoxAccent.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxAccent.setSelected(true);
        jCheckBoxAccent.setText("Выделять точки");
        jCheckBoxAccent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAccentActionPerformed(evt);
            }
        });

        jCheckBoxConnect.setBackground(new java.awt.Color(0, 0, 0));
        jCheckBoxConnect.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxConnect.setSelected(true);
        jCheckBoxConnect.setText("Соединять точки");
        jCheckBoxConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxConnectActionPerformed(evt);
            }
        });

        jButtonChangeColors.setText("Изменить цвета");
        jButtonChangeColors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChangeColorsActionPerformed(evt);
            }
        });

        jButtonSkip.setText("Пропустить");
        jButtonSkip.setEnabled(false);
        jButtonSkip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSkipActionPerformed(evt);
            }
        });

        jButtonFuncGraph.setText("График функции");
        jButtonFuncGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFuncGraphActionPerformed(evt);
            }
        });

        jCheckBoxAutosize.setBackground(new java.awt.Color(0, 0, 0));
        jCheckBoxAutosize.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxAutosize.setSelected(true);
        jCheckBoxAutosize.setText("Автомасштабирование");
        jCheckBoxAutosize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutosizeActionPerformed(evt);
            }
        });

        jButtonHelp.setText("Помощь");
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });

        jButtonConsole.setText("Консоль");
        jButtonConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConsoleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout settingPanelLayout = new javax.swing.GroupLayout(settingPanel);
        settingPanel.setLayout(settingPanelLayout);
        settingPanelLayout.setHorizontalGroup(
            settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel01)
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelAV))
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(settingPanelLayout.createSequentialGroup()
                                        .addComponent(jLabelBeta)
                                        .addGap(51, 51, 51))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingPanelLayout.createSequentialGroup()
                                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabelOmega0)
                                            .addComponent(jLabelF))
                                        .addGap(43, 43, 43)))
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelK)
                                    .addComponent(jLabelM)
                                    .addComponent(jLabelB))))
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonChangeColors, 0, 0, Short.MAX_VALUE)
                                    .addComponent(jButtonOpen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonNew, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonFuncGraph, 0, 0, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonSkip, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonConsole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jSliderdY1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSliderdX2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(jSliderdY2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBoxAutosize)
                                    .addComponent(jCheckBoxAccent)
                                    .addComponent(jCheckBoxFollow)
                                    .addComponent(jCheckBoxConnect)))
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabelCurW))))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(203, Short.MAX_VALUE))
        );
        settingPanelLayout.setVerticalGroup(
            settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jCheckBoxFollow))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxAccent)
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSliderdY1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSliderdY2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jCheckBoxConnect)))
                            .addComponent(jSliderdX2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBoxAutosize)
                        .addContainerGap())
                    .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(settingPanelLayout.createSequentialGroup()
                            .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel01)
                                .addComponent(jButtonNew))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingPanelLayout.createSequentialGroup()
                                    .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelBeta)
                                        .addComponent(jLabelM))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelOmega0)
                                        .addComponent(jLabelK))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelF)
                                        .addComponent(jLabelB)))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingPanelLayout.createSequentialGroup()
                                    .addComponent(jButtonOpen)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButtonChangeColors)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonFuncGraph)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                            .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabelAV)
                                    .addComponent(jLabelCurW)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addComponent(jButtonHelp)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonConsole))
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addComponent(jButtonStart)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonSkip))))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(slickPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1111, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(settingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(slickPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        running = jButtonStart.isSelected();
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jSliderdX2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderdX2StateChanged
        float prev = graph2dX;
        graph2dX = getMultiplier(jSliderdX2.getValue()) / 100f;
        graph2X0 *= prev / graph2dX;
    }//GEN-LAST:event_jSliderdX2StateChanged

    private void jSliderdY2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderdY2StateChanged
        graph2dY = getMultiplier(jSliderdY2.getValue()) / (float) height2;
    }//GEN-LAST:event_jSliderdY2StateChanged

    private void jCheckBoxFollowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFollowActionPerformed
        followingThePoint = jCheckBoxFollow.isSelected();
    }//GEN-LAST:event_jCheckBoxFollowActionPerformed

    private void jSliderdY1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderdY1StateChanged
        graph1dY = getMultiplier(jSliderdY1.getValue()) / (float) height1;
    }//GEN-LAST:event_jSliderdY1StateChanged

    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
        new Physics06OptionsDialog();
    }//GEN-LAST:event_jButtonNewActionPerformed

    private void jButtonOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenActionPerformed
        JFileChooser fc = new JFileChooser("saves/");
        switch (fc.showOpenDialog(this)) {
            case JFileChooser.APPROVE_OPTION:
                fileName = fc.getSelectedFile().getAbsolutePath();
                break;
            default:
                return;
        }
        try {
            loadFromFile();
        } catch (Exception exc) {
            log("Cannot load from file", exc);
            return;
        }
        renew();
        running = false;
        jButtonStart.setSelected(false);
    }//GEN-LAST:event_jButtonOpenActionPerformed

    private void jCheckBoxAccentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAccentActionPerformed
        accentPoints = jCheckBoxAccent.isSelected();
    }//GEN-LAST:event_jCheckBoxAccentActionPerformed

    private void jCheckBoxConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxConnectActionPerformed
        connectGraph = jCheckBoxConnect.isSelected();
    }//GEN-LAST:event_jCheckBoxConnectActionPerformed

    private void jButtonChangeColorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChangeColorsActionPerformed
        new Physics06ColorDialog(this);
    }//GEN-LAST:event_jButtonChangeColorsActionPerformed

    private void jButtonSkipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSkipActionPerformed
        skipW = true;
    }//GEN-LAST:event_jButtonSkipActionPerformed

    private void jButtonFuncGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFuncGraphActionPerformed
        try {
            Runtime.getRuntime().exec("java -jar Physics00FunctionPreview.jar "
                    + F + " " + w);
        } catch (Exception exc) {
            error(exc);
        }
    }//GEN-LAST:event_jButtonFuncGraphActionPerformed

    private void jCheckBoxAutosizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutosizeActionPerformed
        autosize = jCheckBoxAutosize.isSelected();
    }//GEN-LAST:event_jCheckBoxAutosizeActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        try {
//            Runtime.getRuntime().exec("help.html");
            Desktop.getDesktop().open(new File("help/help.html"));
        } catch (IOException e) {
            log("Can't open help file", e);
        }
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jButtonConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConsoleActionPerformed
        new Physics06ConsoleDialog(this);
    }//GEN-LAST:event_jButtonConsoleActionPerformed

    private float getMultiplier(int n) {
        switch (n % 3) {
            case 0:
                return power10[n / 3];
            case 1:
                return 2 * power10[n / 3];
            case 2:
                return 5 * power10[n / 3];
        }
        return 0;
    }

    public void formKeyPressed(java.awt.event.KeyEvent evt) {
        JOptionPane.showMessageDialog(this, "OK");
    }

    public void stop() {
        running = false;
        jButtonStart.setSelected(false);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonChangeColors;
    private javax.swing.JButton jButtonConsole;
    private javax.swing.JButton jButtonFuncGraph;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JButton jButtonOpen;
    private javax.swing.JButton jButtonSkip;
    private javax.swing.JToggleButton jButtonStart;
    private javax.swing.JCheckBox jCheckBoxAccent;
    private javax.swing.JCheckBox jCheckBoxAutosize;
    private javax.swing.JCheckBox jCheckBoxConnect;
    private javax.swing.JCheckBox jCheckBoxFollow;
    private javax.swing.JLabel jLabel01;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelAV;
    private javax.swing.JLabel jLabelB;
    private javax.swing.JLabel jLabelBeta;
    private javax.swing.JLabel jLabelCurW;
    private javax.swing.JLabel jLabelF;
    private javax.swing.JLabel jLabelK;
    private javax.swing.JLabel jLabelM;
    private javax.swing.JLabel jLabelOmega0;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator1;
    public javax.swing.JSlider jSliderdX2;
    public javax.swing.JSlider jSliderdY1;
    public javax.swing.JSlider jSliderdY2;
    private javax.swing.JPanel settingPanel;
    private javax.swing.JPanel slickPanel;
    // End of variables declaration//GEN-END:variables
}
