public class Item {
    public Position pos;
    public char name;

    Item(char name,Position pos){
        this.pos = pos;
        this.name = name;
    }

    // ƒAƒCƒeƒ€‚É‚æ‚Á‚ÄU‚é•‘‚¢‚ğ•Ï‚¦‚½‚¢‚Æ‚«‚Í
    // powerClass‚Æ‚©TamaClass‚Æ‚©ì‚é‚×‚«‚È‚Ì‚©‚à‚µ‚ê‚È‚¢‚ª
    void effect(Player p){
        if (this.name == '—Í') {
            p.power +=1;
        } else if (this.name == '’e') {
            p.setBombLimit +=1;
        }
    }
}
