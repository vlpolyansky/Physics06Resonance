/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physics06;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.newdawn.slick.Color;

/**
 *
 * @author Administrator
 */
public class Physics06Settings {

    static {
    }
    /*
     * Blocks:
     * 0. Constants
     * 1. Common block
     * 2. Resonanse graph
     * 3. Oscillation graph
     * 4. Spring info
     * 5. Colors
     * 6. File IO
     * 7. Search options
     * 8. Settings file
     */
    //----------0. Constants-------------------
    public static float startIdleTime = 1f;
    public static float EPS = 1e-1f;
    public static int lastFileVersion = 1;
    public static int maxFractPow2 = 15;
    public static final ByteArrayOutputStream consoleOut = new ByteArrayOutputStream();
    private static final Logger logger = Logger.getLogger("Logger");
    //-----------------------------------------
    //----------1. Common block----------------
    public static Physics06Frame frame = null;
    public static boolean firstRun = true;
    public static boolean firstIteration = true;
    public static boolean running = false;
    public static boolean autosize = true;
    public static int graphSeparation = 20;
    public static int width = 600, height = 600;
    //-----------------------------------------
    //----------2. Resonance graph-------------
    public static Deque<Vector2f> graphRes = new ArrayDeque<Vector2f>();
    public static Deque<Vector2f> resizeHistory = new ArrayDeque<Vector2f>();
    public static boolean connectGraph = true;
    public static boolean accentPoints = true;
    public static float height1 = 0;
    public static float h1Part = 0.666f;
    public static float leftW = 0.1f;
    public static float rightW = 10;
    public static float chosenLeftW = 0.1f;
    public static float chosenRightW = 10;
    public static float graph1dX = 1f;
    public static float graph1dY = 1f;
    public static float graph1X0 = 0;
    //-----------------------------------------
    //----------3. Oscillation graph-----------
    public static Deque<Vector2f> graphSpring = new ArrayDeque<Vector2f>();
    public static boolean idleProcess = false;
    public static boolean followingThePoint = false;
    public static float height2 = 0;
    public static float time = 0;
    public static float idleTime = 0;
    public static float graph2dX = 0.1f;
    public static float graph2dY = 0.1f;
    public static float graph2X0 = 0f;
    public static float averageDT = 0;
    public static float minA = 0;
    public static float maxA = 0;
    public static float springH = 0;
    public static float springL = 0;
    public static float spring2rToH = 0.1f;
    public static int springN = 0;
    public static int updateCount = 0;
    public static int picWidth = 50;
    public static long prevMillis = 0;
    public static long lastUpdate = -1;
    //-----------------------------------------
    //----------4. Spring info-----------------
    public static Expression ex = null;
    public static String F = "10*cos(w*t)";
    public static float beta = 0;
    public static float w0 = 0;
    public static float m = 1;
    public static float b = 0.1f;
    public static float k = 4f;
    public static float w = 0;
    public static float place = 0;
    public static float V = 0;
    public static float a = 0;
    //-----------------------------------------
    //----------5. Colors----------------------
    public static Color backgroundColor = Color.black;
    public static Color gridRegularColor = Color.darkGray;
    public static Color gridSpecialColor = Color.gray;
    public static Color gridAxisColor = Color.white;
    public static Color selectionColor = new Color(0, 0, 1, 0.10f);
    public static Color shownPointColor = Color.magenta;
    public static Color springColor = Color.green;
    public static Color graphColor = Color.red;
    //-----------------------------------------
    //----------6. File IO---------------------
    public static String[] toSave = {
        "m",
        "k",
        "b",
        "beta",
        "w0",
        "F",
        "leftW",
        "rightW",
        "w",
        "firstIteration",
        "searchType",
        "#graphRes",
        "#wasFract"
    };
    public static String fileName = "saves/temp.ph06";
    public static int fileVersion = 1;
    //-----------------------------------------
    //----------7. Search options--------------
    public static SearchType searchType = SearchType.DELTA;
    public static Deque<Float> userRequestedW = new ArrayDeque<Float>();
    public static boolean[] wasFract = new boolean[(1 << maxFractPow2) + 1];
    public static float[] extremums = new float[100000];
    public static boolean showHOGraph = true;
    public static boolean skipW = false;
    public static float A1_tern = Float.NaN, A2_tern = Float.NaN;
    public static float leftW_tern = leftW, rightW_tern = rightW;
    public static float customDT = 10;
    public static float bufferedW = 0;
    public static int maxExtremums = 100;
    public static int curExtremumCount = 0;
    public static int maxPeriodsNeeded = 3;
    public static int periodCount = 0;
    //-----------------------------------------
    //----------8. Settings file---------------
    //-----------------------------------------
    //================Functions================

    public static void count(float dt) {
        if (idleProcess) {
            idleTime -= dt;
            if (idleTime < 0) {
                refresh();
                if (searchType == SearchType.DELTA) {
                    nextW_DeltaSearch();
                } else {
                    nextW_TernarySearch();
                }
                saveToFile();
                frame.setW(w);
            }
        }
        if (m == 0) {
            return;
        }
        try {
            a = -k * place - V * b + (float) ex.count();
        } catch (Exception exc) {
            log("Exception while changing \'a\' value", exc);
        }
        if (!idleProcess && V >= 0 && V + a * dt <= 0 && a * dt != 0
                || V <= 0 && V + a * dt >= 0 && a * dt != 0) {
            extremums[curExtremumCount++] = place;
            int T = checkForDone();
            frame.setDone(periodCount);
            if (T > 0) {
                minA = maxA = extremums[curExtremumCount - 1];
                for (int i = 1; i < T; i++) {
                    minA = Math.min(minA, extremums[curExtremumCount - 1 - i]);
                    maxA = Math.max(maxA, extremums[curExtremumCount - 1 - i]);
                }
            }
        }
        V += a * dt;
        place += V * dt;
        if (showHOGraph) {
            graphSpring.addLast(new Vector2f(time, place));

            while (2 * place / graph2dY > height2) {
                frame.jSliderdY2.setValue(frame.jSliderdY2.getValue() + 1);
            }
        }
        if (updateCount == 0) {
            updateCount++;
            averageDT = dt;
        } else {
            averageDT = (averageDT * updateCount + dt) / (updateCount + 1);
            updateCount++;
        }
        if (!idleProcess && (periodCount >= maxPeriodsNeeded || skipW)) {
            if (!skipW) {
                if (maxA - minA > EPS) {
                    add(w, (maxA - minA) / 2);
                } else {
                    add(w, Math.abs(maxA));
                }
            } else {
                skipW = false;
            }
            if (searchType == SearchType.TERNARY) {
                if (Float.isNaN(A1_tern)) {
                    A1_tern = (maxA - minA) / 2;
                } else {
                    A2_tern = (maxA - minA) / 2;
                }
            }
            idleTime = startIdleTime;
            idleProcess = true;
        }
        skipW = false;
    }

    public static int checkForDone() {
        periodCount = 0;
        int ans = 0;
        for (int T = 2; T <= maxExtremums && T * 2 <= curExtremumCount; T += 2) {
            for (int i = T; i < curExtremumCount; i++) {
                if (Math.abs(extremums[curExtremumCount - 1 - i]
                        - extremums[curExtremumCount - 1 - (i % T)]) < EPS) {
                    if (periodCount < (i - T + 1) / T) {
                        periodCount = (i - T + 1) / T;
                        ans = T;
                        if (periodCount >= maxPeriodsNeeded) {
                            return ans;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return ans;
    }

    public static void nextW_DeltaSearch() {
        if (!userRequestedW.isEmpty()) {
            w = userRequestedW.pollFirst();
            return;
        }
        if (!wasFract[0]) {
            w = leftW;
            wasFract[0] = true;
            return;
        } else if (!wasFract[wasFract.length - 1]) {
            w = rightW;
            wasFract[wasFract.length - 1] = true;
            return;
        } else {
            for (int i = maxFractPow2; i > 0; i--) {
                for (int j = 0; (1 << (i - 1)) + j * (1 << i) < wasFract.length; j++) {
                    float tmp = leftW + (rightW - leftW) * (1 + 2 * j)
                            / (float) (1 << (maxFractPow2 + 1 - i));
                    if (!wasFract[(1 << (i - 1)) + j * (1 << i)]
                            && tmp >= chosenLeftW && tmp <= chosenRightW) {
                        w = tmp;
                        wasFract[(1 << (i - 1)) + j * (1 << i)] = true;
                        return;
                    }
                }
            }
        }
        frame.stop();
        JOptionPane.showMessageDialog(frame,
                "Максимальная точность на данном отрезке достигнута");
    }

    public static void nextW_TernarySearch() {
        if (!userRequestedW.isEmpty()) {
            w = userRequestedW.pollFirst();
            return;
        }
        if (Float.isNaN(A1_tern)) {
            w = leftW_tern + (rightW_tern - leftW_tern) / 3f;
        } else if (Float.isNaN(A2_tern)) {
            w = leftW_tern + 2f * (rightW_tern - leftW_tern) / 3f;
        } else {
            if (A1_tern < A2_tern) {
                leftW_tern = leftW_tern + (rightW_tern - leftW_tern) / 3f;
            } else if (A1_tern > A2_tern) {
                rightW_tern = leftW_tern + 2 * (rightW_tern - leftW_tern) / 3f;
            } else {
                leftW_tern = leftW_tern + (rightW_tern - leftW_tern) / 3f;
                rightW_tern = leftW_tern + 2 * (rightW_tern - leftW_tern) / 3f;
            }
            A1_tern = Float.NaN;
            A2_tern = Float.NaN;
            nextW_TernarySearch();
        }
    }

    private static void refresh() {
        place = a = V = 0;
        time = 0;
        graphSpring.clear();
        frame.jSliderdY2.setValue(2);
        maxA = minA = 0;
        periodCount = 0;
        graph2X0 = 0;
        idleProcess = false;
        idleTime = 0;
        frame.setDone(0);
        curExtremumCount = 0;
    }

    public static void clear() {
        try {
            ex = new Expression(F);
        } catch (Exception exc) {
            log("Bad expression: " + F, exc);
        }
        leftW_tern = graph1X0 = chosenLeftW = leftW;
        rightW_tern = chosenRightW = rightW;
        resizeHistory.clear();
    }

    private static void add(float x, float y) {
        Deque<Vector2f> tmp = new ArrayDeque<Vector2f>();
        while (!graphRes.isEmpty() && graphRes.getFirst().x < x) {
            tmp.addLast(graphRes.pollFirst());
        }
        tmp.addLast(new Vector2f(x, y));
        while (!graphRes.isEmpty()) {
            tmp.addLast(graphRes.pollFirst());
        }
        graphRes = tmp;
        while (y / graph1dY > height) {
            frame.jSliderdY1.setValue(frame.jSliderdY1.getValue() + 1);
        }
    }

    public static void saveToFile() {
        File old = new File(fileName);
        boolean hasOld = false;
        if (old.exists()) {
            old.renameTo(new File(fileName + ".old"));
            hasOld = true;
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(fileName);
            out.println("fileVersion=" + lastFileVersion);
            for (String fieldName : toSave) {
                out.print(fieldName + "=");
                if (fieldName.startsWith("#")) {
                    out.println();
                    if (fieldName.equals("#graphRes")) {
                        out.println(graphRes.size());
                        for (Vector2f v : graphRes) {
                            out.println(v.x + " " + v.y);
                        }
                    } else if (fieldName.equals("#wasFract")) {
                        for (int i = 0; i < wasFract.length; i++) {
                            if (wasFract[i]) {
                                out.print(i + " ");
                            }
                        }
                        out.println();
                    }
                } else {
                    out.println(Physics06Settings.class.getField(
                            fieldName).get(Physics06Settings.class));
                }
            }
            out.close();
            if (hasOld) {
                hasOld = !(new File(fileName + ".old").delete());
            }
        } catch (Exception exc) {
            log("Exception while saving to file", exc);
            if (out != null) {
                out.close();
            }
            if (hasOld) {
                new File(fileName + ".old").renameTo(old);
            }
        }
    }

    public static void loadFromFile() throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        String s = null;
        while ((s = in.readLine()) != null) {
            if (s.startsWith("#")) {
                if (s.startsWith("#graphRes")) {
                    graphRes.clear();
                    int n = Integer.parseInt(in.readLine());
                    for (int i = 0; i < n; i++) {
                        StringTokenizer tok = new StringTokenizer(in.readLine(), " ");
                        graphRes.add(new Vector2f(
                                Float.parseFloat(tok.nextToken()),
                                Float.parseFloat(tok.nextToken())));
                    }
                } else if (s.startsWith("#wasFract")) {
                    Arrays.fill(wasFract, false);
                    StringTokenizer tok = new StringTokenizer(in.readLine());
                    while (tok.hasMoreTokens()) {
                        wasFract[Integer.parseInt(tok.nextToken())] = true;
                    }
                }
            } else {
                StringTokenizer tok = new StringTokenizer(s, "=");
                String fieldName = tok.nextToken();
                String value = tok.nextToken();
                Field f = Physics06Settings.class.getField(fieldName);
                f.set(Physics06Settings.class, parseObject(f.getType(), value));
            }
        }
        in.close();
        refresh();
        clear();
    }

    private static Object parseObject(Class<?> cl, String s) throws Exception {
        //works with int, float, boolean, String 
        if (cl.getSimpleName().equals("int") || cl.getSimpleName().equals("Integer")) {
            return Integer.parseInt(s);
        } else if (cl.getSimpleName().equalsIgnoreCase("float")) {
            return Float.parseFloat(s);
        } else if (cl.getSimpleName().equals("String")) {
            return s;
        } else if (cl.getSimpleName().equalsIgnoreCase("boolean")) {
            return Boolean.parseBoolean(s);
        } else if (cl.isEnum()) {
            return cl.getMethod("valueOf", String.class).invoke(cl, s);
        } else {
            return null;
        }
    }

    public static void log(Object message, Throwable exc) {
        logger.error(message, exc);
        exc.printStackTrace(System.out);
        JOptionPane.showMessageDialog(frame, message + "\nSee log file or console");
    }

    public static void exec(String command) {
        command = command.trim();
        if (command.length() == 0) {
            return;
        }
        if (command.equalsIgnoreCase("var")) {
            for (Field field : Physics06Settings.class.getFields()) {
                try {
                    System.out.println(field.getType().getName() + " "
                            + field.getName());
                } catch (Exception exc) {
                }
            }
        } else {
            try {
                StringTokenizer tok = new StringTokenizer(command);
                String first = tok.nextToken();
                if (first.equalsIgnoreCase("get")) {
                    String second = tok.nextToken();
                    System.out.println(second + " = " + 
                            Physics06Settings.class.getField(second)
                            .get(Physics06Settings.class));
                } else if (first.equalsIgnoreCase("set")) {
                    String second = tok.nextToken();
                    String value = tok.nextToken();
                    Field f = Physics06Settings.class.getField(second);
                    f.set(Physics06Settings.class, parseObject(f.getType(), value));
                } else if (first.equalsIgnoreCase("clear")) {
                    consoleOut.reset();
                } else if (first.equalsIgnoreCase("help") || first.equals("?")) {
                    System.out.println("Aviable commands:");
                    System.out.println("help or ?\tShow this message");
                    System.out.println("clear\tClears the console");
                    System.out.println("var\tShows fields list");
                    System.out.println("get <field name>\tShows spring representation of"
                            + " the field");
                    System.out.println("set <field name> <value>\tSets the field from"
                            + " the spring representation");
                }
            } catch (Exception e) {
                log("Unknown command: " + command, e);
            }
        }
    }

    public static class Expression {

        private Deque<Elem> deq = new ArrayDeque<Elem>(100);

        public Expression(String s) throws Exception {
            Deque<Elem> op = new ArrayDeque<Elem>(100);
            int l = 0;
            String cur = null;
            boolean isNum = true;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '+' || ch == '-') {
                    add(cur, isNum);
                    cur = null;
                    isNum = true;
                    while (!op.isEmpty() && op.getLast().level == l) {
                        deq.addLast(op.pollLast());
                    }
                    op.add(new Elem(ElemType.FUNC, "" + ch, l));
                } else if (ch == '*' || ch == '/') {
                    add(cur, isNum);
                    cur = null;
                    isNum = true;
                    while (!op.isEmpty() && op.getLast().level == l
                            && !op.getLast().value.equals("+")
                            && !op.getLast().value.equals("-")) {
                        deq.addLast(op.pollLast());
                    }
                    op.add(new Elem(ElemType.FUNC, "" + ch, l));
                } else if (ch == ' ') {
                    continue;
                } else if (ch == '(') {
                    if (cur != null) {
                        op.add(new Elem(ElemType.FUNC, cur, l));
                        cur = null;
                        isNum = true;
                    }
                    l++;
                } else if (ch == ')') {
                    if (cur != null) {
                        add(cur, isNum);
                        cur = null;
                        isNum = false;
                    }
                    while (!op.isEmpty() && op.getLast().level == l) {
                        deq.addLast(op.pollLast());
                    }
                    l--;
                    while (!op.isEmpty() && op.getLast().level == l
                            && !op.getLast().value.equals("+")
                            && !op.getLast().value.equals("-")) {
                        deq.addLast(op.pollLast());
                    }
                } else {
                    if (!Character.isDigit(ch) && ch != '.') {
                        isNum = false;
                    }
                    if (cur == null) {
                        cur = "" + ch;
                    } else {
                        cur += ch;
                    }
                }
            }
            if (cur != null) {
                add(cur, isNum);
                cur = null;
                isNum = true;
            }
            while (!op.isEmpty() && op.getLast().level == l) {
                deq.addLast(op.pollLast());
            }
            count();
        }

        public double count() throws Exception {
            Deque<Double> ans = new ArrayDeque(10);
            for (Elem elem : deq) {
                if (elem.type == ElemType.NUM) {
                    ans.addLast((Double) elem.value);
                } else if (elem.type == ElemType.VAR) {
                    String f = (String) elem.value;
                    if (f.equalsIgnoreCase("t")) {
                        ans.addLast((double) time);
                    } else if (f.equalsIgnoreCase("w")) {
                        ans.addLast((double) w);
                    } else if (f.equalsIgnoreCase("pi")) {
                        ans.addLast(Math.PI);
                    } else {
                        throw new Exception("Unknown variable: \'" + f + "\'");
                    }
                } else {
                    String f = (String) elem.value;
                    if (f.equals("+")) {
                        ans.addLast(ans.pollLast() + ans.pollLast());
                    } else if (f.equals("-")) {
                        ans.addLast(-ans.pollLast() + ans.pollLast());
                    } else if (f.equals("*")) {
                        ans.addLast(ans.pollLast() * ans.pollLast());
                    } else if (f.equals("/")) {
                        ans.addLast(1d / ans.pollLast() * ans.pollLast());
                    } else if (f.equalsIgnoreCase("cos")) {
                        ans.addLast(Math.cos(ans.pollLast()));
                    } else if (f.equalsIgnoreCase("sin")) {
                        ans.addLast(Math.sin(ans.pollLast()));
                    } else if (f.equalsIgnoreCase("tg") || f.equalsIgnoreCase("tan")) {
                        ans.addLast(Math.tan(ans.pollLast()));
                    } else if (f.equalsIgnoreCase("ctg")) {
                        ans.addLast(1d / Math.tan(ans.pollLast()));
                    } else if (f.equalsIgnoreCase("abs")) {
                        ans.addLast(Math.abs(ans.pollLast()));
                    } else if (f.equalsIgnoreCase("sign")) {
                        ans.addLast(Math.signum(ans.pollLast()));
                    } else if (f.equalsIgnoreCase("exp")) {
                        ans.addLast(Math.exp(ans.pollLast()));
                    } else if (f.equalsIgnoreCase("ln")) {
                        ans.addLast(Math.log(ans.pollLast()));
                    } else if (f.equalsIgnoreCase("lg")) {
                        ans.addLast(Math.log10(ans.pollLast()));
                    } else {
                        throw new Exception("Unknown function: \'" + f + "\'");
                    }
                }
            }
            return ans.getFirst();
        }

        private void add(String v, boolean isNum) {
            if (v != null) {
                if (isNum) {
                    deq.add(new Elem(ElemType.NUM, Double.parseDouble(v)));
                } else if (v.equalsIgnoreCase("pi")) {
                    deq.add(new Elem(ElemType.NUM, Math.PI));
                } else {
                    deq.add(new Elem(ElemType.VAR, v));
                }
            } else if (isNum) {
                deq.add(new Elem(ElemType.NUM, 0d));
            }
        }

        @Override
        public String toString() {
            String res = "";
            for (Elem elem : deq) {
                res += elem.value + " ";
            }
            return res;
        }

        private class Elem {

            ElemType type;
            Object value;
            int level = -1;

            public Elem(ElemType type, Object value) {
                this.type = type;
                this.value = value;
            }

            public Elem(ElemType type, Object value, int level) {
                this.type = type;
                this.value = value;
                this.level = level;
            }
        }

        private enum ElemType {

            VAR,
            NUM,
            FUNC
        }
    }

    public static enum SearchType {

        TERNARY,
        DELTA
    }

    public static void error(Exception ex) {
        logger.error(null, ex);
    }

    private Physics06Settings() {
    }
}
