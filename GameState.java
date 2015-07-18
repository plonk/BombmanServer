import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.*;
import java.util.Collections;

public class GameState {
    public static final int INIT_FIRE_POWER = 2;
    public static final int INIT_BOMB_LIMIT = 2;
    public static final int[][] FALLING_WALL =
    {
        {1, 1}, {2, 1}, {3, 1}, {4, 1}, {5, 1}, {6, 1}, {7, 1}, {8, 1}, {9, 1},
        {10, 1}, {11, 1}, {12, 1}, {13, 1}, {13, 2}, {13, 3}, {13, 4}, {13, 5},
        {13, 6}, {13, 7}, {13, 8}, {13, 9}, {13, 10}, {13, 11}, {13, 12},
        {13, 13}, {12, 13}, {11, 13}, {10, 13}, {9, 13}, {8, 13}, {7, 13},
        {6, 13}, {5, 13}, {4, 13}, {3, 13}, {2, 13}, {1, 13}, {1, 12}, {1, 11},
        {1, 10}, {1, 9}, {1, 8}, {1, 7}, {1, 6}, {1, 5}, {1, 4}, {1, 3}, {1, 2},
        {2, 2}, {3, 2}, {4, 2}, {5, 2}, {6, 2}, {7, 2}, {8, 2}, {9, 2}, {10, 2},
        {11, 2}, {12, 2}, {12, 3}, {12, 4}, {12, 5}, {12, 6}, {12, 7}, {12, 8},
        {12, 9}, {12, 10}, {12, 11}, {12, 12}, {11, 12}, {10, 12}, {9, 12},
        {8, 12}, {7, 12}, {6, 12}, {5, 12}, {4, 12}, {3, 12}, {2, 12}, {2, 11},
        {2, 10}, {2, 9}, {2, 8}, {2, 7}, {2, 6}, {2, 5}, {2, 4}, {2, 3}, {3, 3},
        {4, 3}, {5, 3}, {6, 3}, {7, 3}, {8, 3}, {9, 3}, {10, 3}, {11, 3},
        {11, 4}, {11, 5}, {11, 6}, {11, 7}, {11, 8}, {11, 9}, {11, 10}, {11, 11},
        {10, 11}, {9, 11}, {8, 11}, {7, 11}, {6, 11}, {5, 11}, {4, 11}, {3, 11},
        {3, 10}, {3, 9}, {3, 8}, {3, 7}, {3, 6}, {3, 5}, {3, 4}, {4, 4}, {5, 4},
        {6, 4}, {7, 4}, {8, 4}, {9, 4}, {10, 4}, {10, 5}, {10, 6}, {10, 7},
        {10, 8}, {10, 9}, {10, 10}, {9, 10}, {8, 10}, {7, 10}, {6, 10}, {5, 10},
        {4, 10}, {4, 9}, {4, 8}, {4, 7}, {4, 6}, {4, 5}
    };

    public static final String[] DEFAULT_MAP =
    {
        "■■■■■■■■■■■■■■■",
        "■　　　　　　　　　　　　　■",
        "■　■　■　■　■　■　■　■",
        "■　　　　　　　　　　　　　■",
        "■　■　■　■　■　■　■　■",
        "■　　　　　　　　　　　　　■",
        "■　■　■　■　■　■　■　■",
        "■　　　　　　　　　　　　　■",
        "■　■　■　■　■　■　■　■",
        "■　　　　　　　　　　　　　■",
        "■　■　■　■　■　■　■　■",
        "■　　　　　　　　　　　　　■",
        "■　■　■　■　■　■　■　■",
        "■　　　　　　　　　　　　　■",
        "■■■■■■■■■■■■■■■"
    };

    public static final int HEIGHT = DEFAULT_MAP.length;
    public static final int WIDTH = DEFAULT_MAP[0].length();
    public static final int ITEM_COUNT = 20;

    // どう書けばいい？
    public static final char[][] MAP_ARRAY =
    {
        DEFAULT_MAP[0].toCharArray(),
        DEFAULT_MAP[1].toCharArray(),
        DEFAULT_MAP[2].toCharArray(),
        DEFAULT_MAP[3].toCharArray(),
        DEFAULT_MAP[4].toCharArray(),
        DEFAULT_MAP[5].toCharArray(),
        DEFAULT_MAP[6].toCharArray(),
        DEFAULT_MAP[7].toCharArray(),
        DEFAULT_MAP[8].toCharArray(),
        DEFAULT_MAP[9].toCharArray(),
        DEFAULT_MAP[10].toCharArray(),
        DEFAULT_MAP[11].toCharArray(),
        DEFAULT_MAP[12].toCharArray(),
        DEFAULT_MAP[13].toCharArray(),
        DEFAULT_MAP[14].toCharArray()
    };

    // もっと綺麗に書けそうだが書き方がわからない
    public static final Position[] NEAR_INIT_POSITIONS =
    {
        new Position(1,1),
        new Position(1,2),
        new Position(2,1),
        new Position(1,HEIGHT-2),
        new Position(1,HEIGHT-3),
        new Position(2,HEIGHT-2),//左下
        new Position(WIDTH-2,1),
        new Position(WIDTH-2,2),
        new Position(WIDTH-3,1),//右上
        new Position(WIDTH-2,HEIGHT-2),
        new Position(WIDTH-2,HEIGHT-3),
        new Position(WIDTH-3,HEIGHT-2)
    };

    public static final Position[] INIT_POSITIONS =
    {
        new Position(1,1),
        new Position(1,HEIGHT-2),
        new Position(WIDTH-2,1),
        new Position(WIDTH-2,HEIGHT-2)
    };

    int turn;
    ArrayList<Bomb> bombs;
    ArrayList<Player> players;
    ArrayList<Item> items;
    ArrayList<Block> blocks;
    ArrayList<Position> walls;
    ArrayList<Position> fires;

    List<ActionData> actions = new ArrayList<ActionData>();

    // UPDATE処理
    public void update(){
        MapData mapData = toMapData();

        // キャラクタの行動
        actions =
            players.parallelStream()
            .map(p -> p.action(mapData.toJson()))
            .collect(Collectors.toList());
        actions.forEach(action -> evalPutBombAction(action));
        actions.forEach(action -> evalMoveAction(action));

        turn += 1;

        // 壁が落ちてくる
        if (turn >= 360) {
            int i = turn - 360;
            if (i < FALLING_WALL.length) {
                Position p = new Position(FALLING_WALL[i][0], FALLING_WALL[i][1]);
                walls.add(p);
                blocks.removeIf(b -> b.pos.equals(p));
                items.removeIf(item -> item.pos.equals(p));
                bombs.removeIf(b -> {
                        if (b.pos.equals(p)) {
                            b.owner.setBombCount--;
                            return true;
                        } else {
                            return false;
                        }
                    });
            }
        }

        for (Bomb b: bombs) {
            b.timer -=1;
        }

        // get item
        ArrayList<Item> usedItems = new ArrayList<Item>();
        for(Player p: players) {
            for(Item i: items){
                if (p.pos.equals(i.pos)) {
                    i.effect(p);
                    usedItems.add(i);
                }
            }
        }
        items.removeAll(usedItems);


        // bomb explosion
        fires = new ArrayList<Position>();
        ArrayList<Bomb> explodeBombs = new ArrayList<Bomb>();
        for (Bomb b: bombs) {
            if(b.timer <= 0) explodeBombs.add(b);
        }
        // chaining
        while (explodeBombs.size() != 0) {
            explodeBombs.forEach(b-> b.owner.setBombCount -= 1);
            fires.addAll(explodes(explodeBombs));
            bombs.removeAll(explodeBombs);
            explodeBombs = new ArrayList<Bomb>();
            for (Bomb b: bombs) {
                for (Position p: fires){
                    if (b.pos.equals(p)) {
                        explodeBombs.add(b);
                        break;
                    }
                }
            }
        }
        fires = removeDuplicates(fires,(a,b)->a.equals(b));

        // item burning
        items.removeIf(i -> {
                boolean found = false;
                for(Position fire: fires){
                    if(i.pos.equals(fire)) {
                        return true;
                    }
                }
                return false;
            });

        // block burning
        blocks.removeIf(b -> {
                for(Position fire: fires){
                    if(b.pos.equals(fire)) {
                        if (b.item != null) {
                            items.add(b.item);
                        }
                        return true;
                    }
                }
                return false;
            });

        players.forEach(p -> {
                for(Position fire: fires){
                    if(p.pos.equals(fire)) {
                        p.ch = '墓';
                        p.isAlive = false;
                    }
                }
                for(Position fire: walls){
                    if(p.pos.equals(fire)) {
                        p.ch = '墓';
                        p.isAlive = false;
                    }
                }
            });

    }

    MapData toMapData() {
        return new MapData(turn, walls, blocks, players, bombs, items, fires);
    }

    void disposePlayers(){
        players.forEach(p -> p.dispose());
    }

    void newGame(ArrayList<Player> newPlayers) {
        turn = 0;
        bombs = new ArrayList<Bomb>();
        if (players != null) {
            disposePlayers();
        }
        players = newPlayers;
        items = new ArrayList<Item>();
        blocks = new ArrayList<Block>();
        walls = new ArrayList<Position>();
        fires = new ArrayList<Position>();

        // プレイヤーを初期位置に移動
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++) {
            players.get(i).pos = INIT_POSITIONS[i];
            players.get(i).setID(i);
        }

        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < WIDTH; y++){
                if (MAP_ARRAY[y][x] == '■') {
                    walls.add(new Position(x,y));
                }
            }
        }

        while (blocks.size() < 90) {
            Block newBlock = new Block(randomPosition());
            if(!(isNearInitPosition(newBlock.pos))
               && !(isWall(newBlock.pos))
               && !(isBlock(newBlock.pos))){
                blocks.add(newBlock);
            }
        }

        int i = 0;
        for (; i < ITEM_COUNT/2; i++) {
            Block b = blocks.get(i);
            b.item = new Item('力', b.pos);
        }
        for (; i < ITEM_COUNT; i++) {
            Block b = blocks.get(i);
            b.item = new Item('弾', b.pos);
        }
    }

    public void evalPutBombAction(ActionData action){
        try {
            System.out.println(action.toString());
            Player p = action.p;

            if (action.putBomb) {
                Bomb bomb = new Bomb(p);
                boolean existingBomb = false;
                for (Bomb b: bombs) {
                    if (b.pos.equals(bomb.pos)) {
                        existingBomb = true;
                        break;
                    }
                }
                if (p.isAlive
                    && existingBomb == false
                    && p.canSetBomb()) {
                    p.setBombCount += 1;
                    p.totalSetBombCount += 1;
                    bombs.add(bomb);
                }
            }
        } catch(Exception e){
            System.out.println(action.p.name + ": Invalid Action");
        }
    }

    public void evalMoveAction(ActionData action){
        try {
            Player p = action.p;

            Position nextPos = null;
            if (action.dir.equals("UP")) {
                nextPos = new Position(p.pos.x,p.pos.y-1);
            } else if (action.dir.equals("DOWN")) {
                nextPos = new Position(p.pos.x,p.pos.y+1);
            } else if (action.dir.equals("LEFT")) {
                nextPos = new Position(p.pos.x-1,p.pos.y);
            } else if (action.dir.equals("RIGHT")) {
                nextPos = new Position(p.pos.x+1,p.pos.y);
            }

            if (p.isAlive
                && nextPos != null
                && !(isWall(nextPos))
                && !(isBlock(nextPos))
                && !(isBomb(nextPos))) {
                p.pos = nextPos;
            }
        } catch(Exception e){
            System.out.println(action.p.name + ": Invalid Action");
        }
    }

    public static boolean isNearInitPosition(Position pos){
        for (Position p: NEAR_INIT_POSITIONS) {
            if (p.equals(pos)) return true;
        }
        return false;
    }

    public static Position randomPosition(){
        Random rnd = new Random();
        return new Position(rnd.nextInt(WIDTH),rnd.nextInt(HEIGHT));
    }

    public boolean isWall(Position pos) {
        for (Position w: walls) {
            if (w.equals(pos)) return true;
        }
        return false;
    }

    public boolean isBlock(Position pos) {
        for (Block b: blocks) {
            if (b.pos.equals(pos)) return true;
        }
        return false;
    }

    public boolean isItem(Position pos) {
        for (Item i: items) {
            if (i.pos.equals(pos)) return true;
        }
        return false;
    }

    public boolean isBomb(Position pos) {
        for (Bomb b: bombs) {
            if (b.pos.equals(pos)) return true;
        }
        return false;
    }

    public ArrayList<Position> explodes(ArrayList<Bomb> bombs) {
        ArrayList<Position> result = new ArrayList<Position>();
        for(Bomb b: bombs){
            result.addAll(explode(b));
        }
        return result;
    }

    ArrayList<Position> rec(String dir, int p, int power, Bomb bom){
        ArrayList<Position> result = new ArrayList<Position>();
        while (p <= power) {
            Position tmp = (dir == "up")? new Position(bom.pos.x,bom.pos.y-p):
                (dir == "down")? new Position(bom.pos.x,bom.pos.y+p):
                (dir == "left")? new Position(bom.pos.x-p,bom.pos.y):
                new Position(bom.pos.x+p,bom.pos.y);
            if (isWall(tmp)) {
                break;
            } else if (isBlock(tmp) || isItem(tmp)) {
                result.add(tmp);
                break;
            } else {
                result.add(tmp);
                p += 1;
            }
        }
        return result;
    }

    public ArrayList<Position> explode(Bomb bomb) {
        ArrayList<Position> result = new ArrayList<Position>();
        result.add(bomb.pos);
        result.addAll(rec("up",1,bomb.power,bomb));
        result.addAll(rec("down",1,bomb.power,bomb));
        result.addAll(rec("left",1,bomb.power,bomb));
        result.addAll(rec("right",1,bomb.power,bomb));
        return result;
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list,
                                                    BiFunction<T,T,Boolean> equalFn){
        ArrayList<T> result = new ArrayList<T>();
        for(T a : list) {
            boolean found = false;
            for(T b : result) {
                if (equalFn.apply(a,b)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(a);
            }
        }
        return result;
    }
}
