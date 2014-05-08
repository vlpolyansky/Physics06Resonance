/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physics06;

import java.util.Deque;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.MaskUtil;
import static physics06.Physics06Settings.*;

/**
 *
 * @author Administrator
 */
public class Physics06VisState extends BasicGameState {

    private static long[] pow10;
    private UnicodeFont uf;
    private static DecimalFormat df;
    private float curdFollow = 0;
    private boolean pressed1 = false,
            pressed2 = false,
            pressedBtwn = false;
    private int pressedX = 0, pressedY = 0;
    private Rectangle toDraw = null;
    private int curMouseX, curMouseY;
    private float curX, curY;
    private String mouseInfo = null;

    static {
        pow10 = new long[18];
        pow10[0] = 1;
        for (int i = 1; i < pow10.length; i++) {
            pow10[i] = pow10[i - 1] * 10;
        }
        df = new DecimalFormat();
        df.setGroupingUsed(false);
        df.setMaximumIntegerDigits(7);
    }

    public Physics06VisState() throws SlickException {
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        uf = new UnicodeFont(new java.awt.Font("Arial", java.awt.Font.TRUETYPE_FONT, 14));
        uf.getEffects().add(new ColorEffect(java.awt.Color.white));
        uf.addGlyphs("йцукенгшщзхъфывапролджэячсмитьбюЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ"
                + "qwertyuiopasdfghjklzxcvbnnmQWERTYUIOPASDFGHJKLZXCVBNM"
                + "1234567890!@#$%^&*()-=_+[]{};':\'\\|/?.>,< "
                + "²");
        uf.loadGlyphs();
        container.setShowFPS(false);
        container.setAlwaysRender(true);
        container.setSmoothDeltas(true);
        final Input input = container.getInput();
        input.addMouseListener(new MouseListener() {

            @Override
            public void mouseWheelMoved(int change) {
            }

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                if (button == Input.MOUSE_LEFT_BUTTON
                        && x > graphSeparation && x < graphSeparation + width
                        && y > graphSeparation && y < graphSeparation + height1 + 5) {
                    x -= graphSeparation;
                    y -= graphSeparation;
                    Deque<Float> tmp = new ArrayDeque<Float>(userRequestedW.size() + 1);
                    boolean flag = true;
                    int rad = 3;
                    for (Float userW : userRequestedW) {
                        float ux, uy;
                        ux = (userW - graph1X0) / graph1dX;
                        uy = height1 - 2 * getValue(graphRes, userW) / graph1dY;
                        if (Float.isNaN(uy) || !connectGraph) {
                            uy = height1;
                        }
                        if ((x - ux) * (x - ux) + (y - uy) * (y - uy) <= rad * rad) {
                            flag = false;
                        } else {
                            tmp.addLast(userW);
                        }
                    }
                    if (flag) {
                        float uy;
                        uy = height1 - 2 * getValue(graphRes, x * graph1dX + graph1X0)
                                / graph1dY;
                        if (Float.isNaN(uy) || Math.abs(y - uy) <= rad || !connectGraph) {
                            tmp.addLast(x * graph1dX + graph1X0);
                        }
                    }
                    userRequestedW = tmp;
                }
            }

            @Override
            public void mousePressed(int button, int x, int y) {
                if (button == Input.MOUSE_LEFT_BUTTON) {
                    pressedX = x;
                    pressedY = y;
                    if (y < height1 + graphSeparation) {
                        pressed1 = true;
                        if (pressedX < graphSeparation) {
                            pressedX = graphSeparation;
                        }
                    } else if (y > height1 + graphSeparation && y < height1 + 2 * graphSeparation) {
                        pressedBtwn = true;
                    } else if (y > height1 + 2 * graphSeparation) {
                        pressed2 = true;
                    }
                }
            }

            @Override
            public void mouseReleased(int button, int x, int y) {
                if (button == Input.MOUSE_RIGHT_BUTTON && y < height1 + graphSeparation) {
                    if (!resizeHistory.isEmpty()) {
                        Vector2f v = resizeHistory.pollLast();
                        chosenLeftW = v.x;
                        chosenRightW = v.y;
                    } else {
                        chosenLeftW = leftW;
                        chosenRightW = rightW;
                    }
                    graph1X0 = chosenLeftW;
                    frame.resized();
                    return;
                }
                if (toDraw != null && toDraw.getWidth() > 50) {
                    float tmp = chosenLeftW;
                    resizeHistory.addLast(new Vector2f(chosenLeftW, chosenRightW));
                    chosenLeftW = (toDraw.getMinX() - graphSeparation) * graph1dX + tmp;
                    chosenRightW = (toDraw.getMaxX() - graphSeparation) * graph1dX + tmp;
                    graph1X0 = chosenLeftW;
                    frame.resized();
                }
                pressed1 = pressed2 = pressedBtwn = false;
                toDraw = null;
            }

            @Override
            public void mouseMoved(int oldx, int oldy, int newx, int newy) {
            }

            @Override
            public void mouseDragged(int oldx, int oldy, int newx, int newy) {
                if (pressed1) {
                    if (newx < graphSeparation) {
                        newx = graphSeparation;
                    }
                    toDraw = new Rectangle(Math.min(pressedX, newx), graphSeparation,
                            Math.abs(pressedX - newx), height1);
                } else if (pressed2) {
                    graph2X0 += (newx - oldx);
                    if (graph2X0 > 0) {
                        graph2X0 = 0;
                    }
                } else if (pressedBtwn) {
                    int d = newy - oldy;
                    float h = height1 + height2;
                    if (height1 + d < 5) {
                        height1 = 5;
                        height2 = h - 5;
                    } else if (height2 - d < 5) {
                        height1 += h - 5;
                        height2 = 5;
                    } else {
                        height1 += newy - oldy;
                        height2 -= newy - oldy;
                    }
                    h1Part = height1 / (height1 + height2);
                    frame.resized();
                }
            }

            @Override
            public void setInput(Input input) {
            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void inputEnded() {
            }

            @Override
            public void inputStarted() {
            }
        });
        input.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(int key, char c) {
            }

            @Override
            public void keyReleased(int key, char c) {
            }

            @Override
            public void setInput(Input input) {
            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void inputEnded() {
            }

            @Override
            public void inputStarted() {
            }
        });
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        g.setFont(uf);
        g.setColor(backgroundColor);
        g.fillRect(0, 0, container.getWidth(), container.getHeight());
        //-----RESONANCE-GRAPH-----
        g.translate(graphSeparation, graphSeparation);
        drawGraph(g, width - 2 * graphSeparation, height1,
                true, false, graph1dX, graph1dY,
                graphColor, connectGraph, accentPoints,
                graphRes, Math.round(-chosenLeftW / graph1dX));
        if (toDraw != null) {
            g.setColor(selectionColor);
            g.fillRect(toDraw.getX() - graphSeparation, toDraw.getY() - graphSeparation,
                    toDraw.getWidth(), toDraw.getHeight());
        }
        g.setColor(shownPointColor);
        if (mouseInfo != null && connectGraph) {
            int rad = 3;
            float x, y;
            x = curMouseX - graphSeparation;
            y = height1 - 2 * curY / graph1dY;
            g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad);
            g.drawString(mouseInfo, x, y - g.getFont().getLineHeight() - rad);
        }
        for (Float userW : userRequestedW) {
            int rad = 3;
            float x, y;
            x = (userW - graph1X0) / graph1dX;
            float tmp = getValue(graphRes, userW);
            if (Float.isNaN(tmp) || !connectGraph) {
                tmp = 0;
            }
            y = height1 - 2 * tmp / graph1dY;
            g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad);
        }
        {
            int rad = 3;
            g.setColor(springColor);
            float x, y;
            x = (w - graph1X0) / graph1dX;
            float tmp = getValue(graphRes, w);
            if (Float.isNaN(tmp) || !connectGraph) {
                tmp = 0;
            }
            y = height1 - 2 * tmp / graph1dY;
            g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad);
        }
        g.translate(-graphSeparation, 0);
        //--------------------------
        float startX = picWidth;
        graph2X0 = Math.round(graph2X0);
        g.translate(0, graphSeparation + height1);
        //spring
        g.setColor(backgroundColor);
        g.fillRect(0, 0, startX - 1, height2 + graphSeparation);
        springN = (int) height2 / 20;
        springH = height2 / 2;
        spring2rToH = picWidth / (2 * springH);
        springL = (float) (2 * springN * springH * Math.sqrt(1.0 / (4.0 * springN * springN)
                + spring2rToH * spring2rToH));
        g.setColor(springColor);
        g.setLineWidth(2);
        g.drawLine(0, 0, picWidth, 0);
        for (float x = 0; x <= picWidth; x += picWidth / 5f) {
            g.drawLine(x, 0, x + 5f, -5f);
        }
        g.setLineWidth(1);
        float curH = springH - place / graph2dY;
        float curSpringY = 0;
        float dy = curH / springN;
        float dlen = Math.signum(dy) * Math.max(springL / springN, dy * Math.signum(dy));
        float dx = (float) Math.sqrt(dlen * dlen - dy * dy) / 4;
        for (int j = 0; j < springN; j++) {
            g.drawLine((0.5f) * picWidth + 0, curSpringY + j * dy,
                    (0.5f) * picWidth + dx, curSpringY + (j + 0.25f) * dy);
            g.drawLine((0.5f) * picWidth + dx, curSpringY + (j + 0.25f) * dy,
                    (0.5f) * picWidth + -dx, curSpringY + (j + 0.75f) * dy);
            g.drawLine((0.5f) * picWidth + -dx, curSpringY + (j + 0.75f) * dy,
                    (0.5f) * picWidth + 0, curSpringY + (j + 1) * dy);
        }
        g.drawRect((0.1f) * picWidth,
                curH - 0.33f * (0.8f) * picWidth,
                0.8f * picWidth,
                0.66f * 0.8f * picWidth);
        g.setColor(new Color(springColor.r, springColor.g,
                springColor.b, 0.5f));
        g.fillRect((0.1f) * picWidth,
                curH - 0.33f * (0.8f) * picWidth,
                0.8f * picWidth,
                0.66f * 0.8f * picWidth);
        g.setColor(graphColor);
        int rad = 4;
        g.fillOval((0.5f) * picWidth - rad, curH - rad, 2 * rad, 2 * rad);
        //Graph
        g.translate(startX, 0);
        drawGraph(g, width, height2,
                true, true, graph2dX, graph2dY, graphColor,
                true, false,
                graphSpring, graph2X0);

    }

    private void drawGraph(Graphics g, float width, float height,
            boolean drawGrid, boolean centeredY, float dx, float dy, Color c,
            boolean connect, boolean accent,
            Deque<Vector2f> graph, float x0) {
        if (drawGrid) {
            g.setLineWidth(1);
            for (float x = x0 + 0, num = 0; x <= width; x += 1, num += dx) {
                if (x < 0) {
                    continue;
                }
                if ((x - x0) % 100 <= 1e-5) {
                    g.setColor(gridSpecialColor);
                    g.setLineWidth(2);
                    g.drawString(format((x - x0 + 1e-2) * dx, 2), x,
                            (centeredY ? height / 2 : height));
                    g.drawLine(x, 1, x, height - 1);
                } else if ((x - x0) % 10 == 0) {
                    g.setColor(gridRegularColor);
                    g.setLineWidth(1);
                    g.drawLine(x, 0, x, height);
                } else {
                    continue;
                }
            }
            g.setColor(gridSpecialColor);
            g.setLineWidth(1);
            for (float y = 0, num = height * dy / 2; y <= height + 1;
                    y += height / 8f, num -= height * dy / (centeredY ? 8f : 16f)) {
                if (Math.abs(num) > 1e-5) {
                    g.drawString(format(num + 1e-4, 2), 0, y);
                }
                g.drawLine(0, y, width, y);
            }
            g.setColor(gridAxisColor);
            g.setLineWidth(3);
            g.drawLine(0, (centeredY ? height / 2 : height),
                    width, (centeredY ? height / 2 : height));
            g.setLineWidth(2);
        }
        MaskUtil.defineMask();
        g.fillRect(0, 0, width, height);
        MaskUtil.finishDefineMask();
        MaskUtil.drawOnMask();
        g.setColor(c);
        Vector2f prev = null;
        for (Vector2f p : graph) {
            if (!accent && !connect) {
                int rad = 1;
                g.fillOval(x0 + p.x / dx - rad,
                        (centeredY ? 1 : 2) * (height / 2 - p.y / dy) - rad,
                        2 * rad, 2 * rad);
            }
            if (accent) {
                int rad = 3;
                g.fillOval(x0 + p.x / dx - rad,
                        (centeredY ? 1 : 2) * (height / 2 - p.y / dy) - rad,
                        2 * rad, 2 * rad);
            }
            if (prev == null) {
                prev = p;
                continue;
            }
            if (p.x / dx + x0 < 0) {
//            } else if (prev.x / dx + x0 < 0) {
            } else {
                if (connect) {
                    g.drawLine(x0 + prev.x / dx, (centeredY ? 1 : 2) * (height / 2 - prev.y / dy),
                            x0 + p.x / dx, (centeredY ? 1 : 2) * (height / 2 - p.y / dy));
                }
            }
            prev = p;
        }

        MaskUtil.resetMask();
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        curMouseX = input.getMouseX();
        curMouseY = input.getMouseY();
        lastUpdate = System.currentTimeMillis();
        float dt = (lastUpdate - prevMillis) / 1000f;
        if (customDT != 0) {
            dt = customDT / 1000f;
        }
        prevMillis = lastUpdate;
        if (running) {
            time += dt;
            count(dt);
            if (followingThePoint) {
                curdFollow += dt;
                graph2X0 -= Math.round(curdFollow / graph2dX);
                curdFollow -= Math.round(curdFollow / graph2dX) * graph2dX;
            }
            if (customDT == 0) {
                frame.setAV(averageDT);
            }
        }
        if (curMouseX > graphSeparation && curMouseX < graphSeparation + width
                && curMouseY > graphSeparation && curMouseY < graphSeparation + height1) {
            float x = (curMouseX - graphSeparation) * graph1dX + graph1X0;
            float y = getValue(graphRes, x);
            if (!Float.isNaN(y)) {
                mouseInfo = "(" + format(x, 2) + "; " + format(y, 2) + ")";
                curX = x;
                curY = y;
            } else {
                mouseInfo = null;
            }
        } else {
            mouseInfo = null;
        }
    }

    private float getValue(Deque<Vector2f> graph, float x) {
        if (graph.isEmpty()) {
            return Float.NaN;
        }
        if (x < graph.getFirst().x || x > graph.getLast().x) {
            return Float.NaN;
        }
        Vector2f prev = null;
        for (Vector2f p : graph) {
            if (prev == null || x > p.x) {
                prev = p;
            } else {
                return (x - prev.x) * (p.y - prev.y) / (p.x - prev.x) + prev.y;
            }
        }
        return Float.NaN;
    }

    public static String format(double num, int d) {
        if (Math.abs(num) < 1e6) {
            df.setMinimumFractionDigits(d);
            df.setMaximumFractionDigits(d);
            return df.format(num);
        } else {
            return Double.toString(num);
        }
    }

    public static String format_(float num, int d) {
        if (num != num) {
            return "NaN";
        }
        if (d == 0) {
            return "" + (int) num;
        }
        int n = (int) ((num > 0 ? num : -num) * pow10[d]);
        String start = null;
        if (num < 0) {
            start = "-";
        } else {
            start = "";
        }
        String back = "";
        for (int i = 0; i < d; i++) {
            back = (n % 10) + back;
            n /= 10;
        }
        return start + n + "." + back;
    }
}
