import java.util.ResourceBundle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.UIManager;
import javax.swing.UIManager.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.EventQueue;

class You extends Player {
    public transient String direction;
    public transient boolean[] keyStates;
    public transient boolean putBomb;

    You(String name) {
        super(name);
        direction = "";
        keyStates =  new boolean[5];//４方向（上=0,下=1,左=2,右=3）＋Ｚキー=4
        putBomb = false;
    }
    public ActionData action(String mapData){
        String nextMove = "STAY";
        if (direction == "UP" || keyStates[0]) {
            nextMove = "UP";
        } else if (direction == "DOWN" || keyStates[1]) {
            nextMove = "DOWN";
        } else if (direction == "LEFT" || keyStates[2]) {
            nextMove = "LEFT";
        } else if (direction == "RIGHT" || keyStates[3]) {
            nextMove = "RIGHT";
        }
        ActionData result =
            new ActionData(this,nextMove,putBomb);
        direction = "";
        putBomb = false;
        return result;
    }
}

class PerformanceCounter {
    long sumNanos;
    long nDataPoints;
    long maxNanos = Long.MIN_VALUE;
    long minNanos = Long.MAX_VALUE;

    void addNanos(long elapsedNanos){
        maxNanos = Math.max(elapsedNanos, maxNanos);
        minNanos = Math.min(elapsedNanos, minNanos);
        sumNanos += elapsedNanos;
        nDataPoints += 1;
    }

    long getMinNanos(){
        if (nDataPoints == 0) throw new RuntimeException("no data points");
        return minNanos;
    }

    long getMaxNanos(){
        if (nDataPoints == 0) throw new RuntimeException("no data points");
        return maxNanos;
    }

    long getAvgNanos(){
        if (nDataPoints == 0) throw new RuntimeException("no data points");
        return sumNanos / nDataPoints;
    }
}


class ExAI extends Player {
    transient BufferedWriter writer;
    transient BufferedReader reader;
    transient BufferedReader errorReader;
    transient Process proc;
    transient PerformanceCounter perf;

    ExAI(String command){
        super("未接続");
        perf = new PerformanceCounter();
        try {
            proc = Runtime.getRuntime().exec(command);
            writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(),"UTF-8"));
            reader = new BufferedReader(new InputStreamReader(proc.getInputStream(),"UTF-8"));
            errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream(),"UTF-8"));

            // 標準エラー出力はサーバの標準出力に垂れ流す
            new Thread(new Runnable(){
                    public void run(){
                        try {
                            for (String line = errorReader.readLine();
                                 line != null;
                                 line = errorReader.readLine()) {
                               System.out.println(line);
                            }
                        }catch(Exception e){
                        }
                    }
                }).start();
            this.name = reader.readLine();
            this.ch = name.charAt(0);
        } catch (Exception e) {
            System.out.println(e);
            this.ch = '落';
        }
    }

    public ActionData action(String mapData){
        long startTime = System.nanoTime();
        ActionData ret = doAction(mapData);
        long elapsedNanos = System.nanoTime() - startTime;
        long elapsedMillis = elapsedNanos / 1000 / 1000;

        perf.addNanos(elapsedNanos);
        if (elapsedMillis >= 500) {
            System.out.println("警告: " + this.name + "が応答に" + elapsedMillis + "ミリ秒かかりました。");
        }
        return ret;
    }

    public ActionData doAction(String mapData){
        try {
            writer.write(mapData+"\n");
            writer.flush();
            String raw = reader.readLine();
            System.out.println("RAW: " + this.name + ": "+raw);
            String[] data = raw.split(",",3);
            if (data.length == 3) {
                return new ActionData(this,data[0],Boolean.valueOf(data[1]), data[2]);
            } else {
                return new ActionData(this,data[0],Boolean.valueOf(data[1]));
            }
        } catch(Exception e) {
            System.out.println(e);
            this.ch = '落';
            return new ActionData(this,"STAY",false);
        }
    }

    public void setID(int id){
        super.setID(id);
        try {
            writer.write(id+"\n");
            writer.flush();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void dispose(){
        try {
            if (writer != null) {
                System.out.println(this.name + "との接続を切断しています。");
                writer.close();
                writer = null;
            }
            if (proc != null) {
                System.out.println(this.name + "の終了を待っています。");
                proc.waitFor();
                System.out.println(this.name + "が終了しました。");
                proc = null;
            }
            String msg = String.format("%s パフォーマンス min/avg/max %.3f/%.3f/%.3f ms",
                                       this.name,
                                       (double) perf.getMinNanos() / 1000 / 1000,
                                       (double) perf.getAvgNanos() / 1000 / 1000,
                                       (double) perf.getMaxNanos() / 1000 / 1000);
            System.out.println(msg);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

class AIPlayer extends Player {
    transient Random rand;
    AIPlayer(String name){
        super(name);
        rand = new Random();
    }

    public ActionData action(String mapData){
        String[] moves = {"UP","DOWN","LEFT","RIGHT"};
        return new ActionData(this,moves[rand.nextInt(moves.length)],false);
    }
}

class ActionData {
    public Player p;
    public String dir;
    public boolean putBomb;
    public String message = "";

    ActionData (Player p,String dir,boolean putBomb){
        this.p = p;
        this.dir = dir;
        this.putBomb = putBomb;
    }

    ActionData (Player p,String dir,boolean putBomb, String message){
        this.p = p;
        this.dir = dir;
        this.putBomb = putBomb;
        this.message = message;
    }

    public String toString(){
        return p.name +": " + dir +"," + putBomb;
    }
}

public class BombmanServer {
    public static final String VERSION = "0.4.6";

    static final int DEFAULT_SLEEP_TIME = 500;

    int showTurn;
    int sleepTime = DEFAULT_SLEEP_TIME;

    JTextPane field;
    JTextArea textArea;
    JTextArea infoArea;
    JScrollPane scrollpane;
    JCheckBox stopCheckBox;

    boolean putBomb = false;
    You you;
    String direction = "";

    Timer timer;
    TimerTask task;

    ArrayList<String> history;

    GameState gameState;

    void configureLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
    }

    BombmanServer(){
        configureLookAndFeel();
        gameState = new GameState();

        JFrame frame = new JFrame("ボムマン "+VERSION);
        frame.setBounds(100, 100, 500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent winEvt) {
                    task.cancel();
                    gameState.disposePlayers();
                }
            });

        field = new JTextPane();
        infoArea = new JTextArea();
        infoArea.setColumns(10);
        infoArea.setEditable(false);
        textArea = new JTextArea();
        textArea.setRows(5);
        scrollpane = new JScrollPane(textArea,
                                     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setFontFamily(a,Font.MONOSPACED);
        StyleConstants.setFontSize(a,18);
        StyleConstants.setLineSpacing(a, -0.15f);
        field.setParagraphAttributes(a, true);
        field.setEditable(false);

        field.addKeyListener(new KeyListener(){
                @Override
                public void keyPressed(KeyEvent e){
                    int key = e.getKeyCode();
                    switch(key){
                    case KeyEvent.VK_UP:
                        if(you.keyStates[0] == false){
                            you.direction = "UP";
                            you.keyStates[0] = true;
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if(you.keyStates[1] == false){
                            you.direction = "DOWN";
                            you.keyStates[1] = true;
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        if(you.keyStates[2] == false){
                            you.direction = "LEFT";
                            you.keyStates[2] = true;
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if(you.keyStates[3] == false){
                            you.direction = "RIGHT";
                            you.keyStates[3] = true;
                        }
                        break;
                    case KeyEvent.VK_SPACE:
                        you.putBomb = true;
                        break;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    int key = e.getKeyCode();
                    switch(key){
                    case KeyEvent.VK_UP:
                        you.keyStates[0] = false;
                        break;
                    case KeyEvent.VK_DOWN:
                        you.keyStates[1] = false;
                        break;
                    case KeyEvent.VK_LEFT:
                        you.keyStates[2] = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                        you.keyStates[3] = false;
                        break;
                    }
                }
                @Override
                public void keyTyped(KeyEvent e) {
                }
            });

        JButton prev2     = new JButton("<<");
        JButton prev      = new JButton("<");
        JButton next      = new JButton(">");
        JButton next2     = new JButton(">>");
        JButton stop      = new JButton("停止");
        JButton play      = new JButton("再生");
        JButton fast      = new JButton("早送り");
        JButton superFast = new JButton("超早送り");
        JButton retry     = new JButton("もう一戦");
        stopCheckBox = new JCheckBox("勝敗が決まったら止める",true);
        prev.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                    showTurn -= 1;
                    if (showTurn < 0) {
                        showTurn = 0;
                    }
                    showMap(MapData.fromJson(history.get(showTurn)));
                }
            });
        prev2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                    showTurn -= 10;
                    if (showTurn < 0) {
                        showTurn = 0;
                    }
                    showMap(MapData.fromJson(history.get(showTurn)));
                }
            });
        next.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                    showTurn += 1;
                    if (showTurn > gameState.turn) {
                        new UpdateTask().run();
                    } else {
                        showMap(MapData.fromJson(history.get(showTurn)));
                    }
                }
            });
        next2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                    showTurn += 10;
                    if (showTurn > gameState.turn) {
                        new UpdateTask().run();
                    } else {
                        showMap(MapData.fromJson(history.get(showTurn)));
                    }
                }
            });
        stop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                }
            });
        play.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                    task = new UpdateTask();
                    timer.schedule(task, 0, DEFAULT_SLEEP_TIME);
                }});
        fast.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                    task = new UpdateTask();
                    timer.schedule(task, 0, 100);
                }});
        superFast.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                    task = new UpdateTask();
                    timer.schedule(task, 0, 1);
                }});
        retry.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    task.cancel();
                    newGame();
                }});

        JPanel buttons1 = new JPanel();
        JPanel buttons2 = new JPanel();
        JPanel buttons = new JPanel();
        buttons1.setLayout(new FlowLayout());
        buttons1.add(prev2);
        buttons1.add(prev);
        buttons1.add(next);
        buttons1.add(next2);
        buttons1.add(stopCheckBox);
        buttons2.add(stop);
        buttons2.add(play);
        buttons2.add(fast);
        buttons2.add(superFast);
        buttons2.add(retry);
        buttons.setLayout(new BorderLayout());
        buttons.add(buttons1, BorderLayout.NORTH);
        buttons.add(buttons2, BorderLayout.SOUTH);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(buttons, BorderLayout.NORTH);
        frame.getContentPane().add(field, BorderLayout.CENTER);
        frame.getContentPane().add(scrollpane, BorderLayout.SOUTH);
        frame.getContentPane().add(infoArea, BorderLayout.EAST);
        frame.setVisible(true);

        EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    field.requestFocusInWindow();
                }
            });
    }

    void newGame() {
        ArrayList<Player> players = new ArrayList<Player>();

        you = new You("あなた");
        textArea.setText("");

        ResourceBundle rb = ResourceBundle.getBundle("bombman");
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add(rb.getString("ai0"));
        tmp.add(rb.getString("ai1"));
        tmp.add(rb.getString("ai2"));
        tmp.add(rb.getString("ai3"));
        tmp.removeIf(s-> s.trim().equals(""));
        tmp.forEach(s -> players.add(new ExAI(s)));

        if (players.size() < 4){
            players.add(you);
        }

        while (players.size() < 4) {
            players.add(new AIPlayer("敵"));
        }
        gameState.newGame(players);

        history = new ArrayList<String>();
        MapData mapData = gameState.toMapData();
        history.add(mapData.toJson());

        showMap(mapData);
        textArea.append("TURN 0: ゲームが開始されました\n");

        timer = new Timer();
        task = new UpdateTask();
        timer.schedule(task, 1000, DEFAULT_SLEEP_TIME);
    }

    void showMap(MapData mapData){
        String mapString = mapToString(mapData);

        updateText(mapString);
        printAsJson(mapData);
    }

    void updateText(String mapString) {
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, Color.RED);
        ArrayList<int[]> firePos = findFireIndex(mapString);
        field.setText(mapString);
        StyledDocument doc = (StyledDocument) field.getDocument();
        firePos.forEach(p -> {
                doc.setCharacterAttributes(p[0], p[1], attr, true);
            });

        StringBuffer result = new StringBuffer();
        gameState.players.forEach(p -> {
                result.append(p.name + "\n"
                              + "力:" + p.power + " 弾:" + p.setBombLimit
                              + " 計:" + p.totalSetBombCount
                              + "\n\n");
            });
        infoArea.setText(result.toString());
    }

    void printMap(String mapString) {
        System.out.print(mapString);
    }

    void printAsJson(MapData mapData) {
        System.out.println(mapData.toJson() + "\n");
    }

    class UpdateTask extends TimerTask {
        public void run(){
            gameState.update();

            for (ActionData action: gameState.actions) {
                if (!action.message.equals("")) {
                    textArea.append(action.p.name + "「" + action.message + "」\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }
            }


            showTurn = gameState.turn;

            // マップデータをヒストリーに登録
            MapData mapData = gameState.toMapData();
            history.add(mapData.toJson());
            showMap(mapData);
            
            List<Player> living = gameState.players.stream().filter(p -> p.isAlive)
                .collect(Collectors.toList());

            if(living.size() == 1){
                textArea.append("TURN " + gameState.turn + " "
                                + living.get(0).name
                                + "の勝ちです！\n");
                if (stopCheckBox.isSelected()){
                    this.cancel();
                }
            } else if (living.size() == 0){
                textArea.append("引き分けです！\n");
                if (stopCheckBox.isSelected()){
                    this.cancel();
                }
            }
        }
    }

    public static void fill2(char[][] ary,char a) {
        for(int i = 0; i < ary.length; i++){
            for(int j = 0; j < ary[0].length; j++){
                ary[i][j] = a;
            }
        }
    }

    public static <T> void fill2(T[][] ary,T a) {
        for(int i = 0; i < ary.length; i++){
            for(int j = 0; j < ary[0].length; j++){
                ary[i][j] = a;
            }
        }
    }

    public static String mapToString(MapData map){
        char[][] mapArray = new char[GameState.HEIGHT][GameState.WIDTH];

        fill2(mapArray, '　');

        for (int[] b: map.blocks) {
            mapArray[b[1]][b[0]] = '□';
        }

        for (Bomb b: map.bombs) {
            mapArray[b.pos.y][b.pos.x] = '●';
        }

        for (Item i: map.items) {
            mapArray[i.pos.y][i.pos.x] = i.name;
        }

        for (int[] f: map.fires) {
            mapArray[f[1]][f[0]] = '火';
        }

        for (int[] p: map.walls) {
            mapArray[p[1]][p[0]] = '■';
        }

        for (Player p: map.players) {
            mapArray[p.pos.y][p.pos.x] = p.ch;
        }

        StringBuffer result = new StringBuffer();
        for(int y = 0; y < GameState.HEIGHT; y++){
            for(int x = 0; x < GameState.WIDTH; x++){
                result.append(mapArray[y][x]);
            }
            result.append('\n');
        }
        return "Turn " + map.turn + "\n" + result.toString();
    }

    static ArrayList<int[]> findFireIndex(String str){
        ArrayList<int[]> result = new ArrayList<int[]>();
        int len = str.length();
        boolean found = false;
        int start = 0;
        for(int i = 0; i < len; i++){
            if (str.charAt(i) == '火') {
                if (found) {
                } else {
                    found = true;
                    start = i;
                }
            } else {
                if (found) {
                    result.add(new int[]{start,i-start});
                }
                found = false;
            }
        }
        return result;
    }

    public static void main(String[] args){
        BombmanServer bs = new BombmanServer();
        bs.newGame();
    }
}
