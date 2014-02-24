/**
 * Created by Mike on 2/16/14.
 */
import edu.cwru.sepia.util.Pair;

public class ABUnit {

    private int id;
    private Pair<Integer, Integer> pos;

    public ABUnit(ABUnit baseUnit) {
        id = baseUnit.getId();
        pos = baseUnit.getPos();
    }

    public ABUnit(int id, int x, int y) {
        this.id = id;
        pos = new Pair<Integer, Integer>(x, y);
    }

    public int getId() { return id; }

    public Pair<Integer, Integer> getPos() { return new Pair<Integer, Integer>(pos.a, pos.b); }

    public int getX() { return pos.a; }

    public int getY() { return pos.b; }

    public void setX(int x) { pos.a = x; }

    public void setY(int y) { pos.b = y; }

    @Override
    public String toString() {
        return id + "(" + pos.a + "," + pos.b + ")";
    }
}
