public class Bomb {
    public static final int EXPLODE_TIMER = 10;
    public Position pos;
    public int timer;
    public int power;
    public transient Player owner;

    Bomb(Player owner) {
        this.pos = owner.pos; // pos ha immutable
        this.power = owner.power;
        this.timer = EXPLODE_TIMER;
        this.owner = owner;
    }
    public String toString(){
        return "[" + pos.x + "," + pos.y + "]";
    }
}
