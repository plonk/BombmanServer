import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class MapData {
    int turn;
    List<int[]> walls;
    List<int[]> blocks;
    List<Player> players;
    List<Bomb> bombs;
    List<Item> items;
    List<int[]> fires;

    transient static Gson gson = new Gson();

    static MapData fromJson(String json) {
        return gson.fromJson(json, MapData.class);
    }

    public MapData(int turn,
                   List<Position> walls,
                   List<Block> blocks,
                   List<Player> players,
                   List<Bomb> bombs,
                   List<Item> items,
                   List<Position> fires) {
        this.turn = turn;
        this.walls = walls.stream()
            .map(p -> new int[] { p.x, p.y })
            .collect(Collectors.toList());
        this.blocks =
            blocks.stream()
            .map(b -> new int[] { b.pos.x, b.pos.y })
            .collect(Collectors.toList());
        this.players = players;
        this.bombs = bombs;
        this.items = items;
        this.fires =
            fires.stream()
            .map(f -> new int[] { f.x, f.y })
            .collect(Collectors.toList());
    }

    String toJson() {
        return gson.toJson(this);
    }
}
