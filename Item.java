public class Item {
    public Position pos;
    public char name;

    Item(char name,Position pos){
        this.pos = pos;
        this.name = name;
    }

    // �A�C�e���ɂ���ĐU�镑����ς������Ƃ���
    // powerClass�Ƃ�TamaClass�Ƃ����ׂ��Ȃ̂�������Ȃ���
    void effect(Player p){
        if (this.name == '��') {
            p.power +=1;
        } else if (this.name == '�e') {
            p.setBombLimit +=1;
        }
    }
}
