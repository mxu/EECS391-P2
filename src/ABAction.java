/**
 * Created by Mike on 2/15/14.
 * ABAgent state transition representations
 */
public enum ABAction {
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    ATTACK;

    public static void main(String[] args) {
        for(ABAction s: ABAction.values()) System.out.println(s);
    }
}
