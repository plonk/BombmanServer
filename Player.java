public class Player {
    public static final int DEFAULT_POWER = 2;
    public static final int DEFAULT_BOMB_LIMIT = 2;
    public String name;
    public Position pos;
    public int power;
    public int setBombLimit;
    public char ch;
    public boolean isAlive;
    public int setBombCount;
    public int totalSetBombCount;
    public int id;

    Player(String name) {
        this.name = name;
        this.power = DEFAULT_POWER;
        this.setBombLimit = DEFAULT_BOMB_LIMIT;
        this.ch = name.charAt(0);
        this.isAlive = true;
        this.setBombCount = 0;
    }

    public boolean canSetBomb(){
        return setBombCount < setBombLimit;
    }

    public ActionData action(String mapdata) {
        return new ActionData(this,"STAY",false);
    }

    public void setID(int id){
        this.id = id;
    }

    public void dispose() {}
}
