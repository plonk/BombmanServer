public class Item {
    public Position pos;
    public char name;

    Item(char name,Position pos){
        this.pos = pos;
        this.name = name;
    }

    // アイテムによって振る舞いを変えたいときは
    // powerClassとかTamaClassとか作るべきなのかもしれないが
    void effect(Player p){
        if (this.name == '力') {
            p.power +=1;
        } else if (this.name == '弾') {
            p.setBombLimit +=1;
        }
    }
}
