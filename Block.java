public class Block {
    public Position pos;
    transient Item item;
    Block(Position pos){
        this.pos = pos;
    }
    public boolean equal(Block b){
        return this.pos.equals(b.pos);
    }
}
