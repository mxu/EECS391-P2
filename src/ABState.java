import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

import java.util.*;

/**
 * Created by Mike on 2/15/14.
 */
public class ABState {

    private LinkedList<ABUnit> footmen;
    private LinkedList<ABUnit> archers;
    private HashMap<Integer, ABAction> parentAction;
    private Integer score;

    // constructor for creating a blank state with given score
    // used for initializing alpha and beta to +/- infinity
    public ABState(Integer score) {
        footmen = new LinkedList<ABUnit>();
        archers = new LinkedList<ABUnit>();
        this.score = score;
    }

    // constructor for creating a new state by applying action to given state
    public ABState(ABState baseState, HashMap<Integer, ABAction> action, boolean isMax) {
        this(baseState);
        apply(action, isMax);
    }

    // constructor for creating a new copy of another state
    public ABState(ABState baseState) {
        footmen = new LinkedList<ABUnit>();
        for(ABUnit f: baseState.getFootmen())
            footmen.add(new ABUnit(f));
        archers = new LinkedList<ABUnit>();
        for(ABUnit a: baseState.getArchers())
            archers.add(new ABUnit(a));
    }

    // constructor for creating a new state from a game state
    public ABState(State.StateView state) {
        footmen = new LinkedList<ABUnit>();
        archers = new LinkedList<ABUnit>();
        for(int id: state.getAllUnitIds()) {
            Unit.UnitView unit = state.getUnit(id);
            ABUnit abUnit = new ABUnit(id, unit.getXPosition(), unit.getYPosition());
            String type = unit.getTemplateView().getName();
            if(type.equals("Footman")) footmen.add(abUnit);
            if(type.equals("Archer")) archers.add(abUnit);
        }
    }

    // apply actions to units for this turn
    public ABState apply(HashMap<Integer, ABAction> am, boolean isMax) {
        parentAction = am;
        for(ABUnit u: isMax ? footmen : archers) {
            if(parentAction.containsKey(u.getId())) {
                switch(parentAction.get(u.getId())) {
                    case MOVE_UP:
                        u.setY(u.getY() - 1);
                        break;
                    case MOVE_DOWN:
                        u.setY(u.getY() + 1);
                        break;
                    case MOVE_LEFT:
                        u.setX(u.getX() - 1);
                        break;
                    case MOVE_RIGHT:
                        u.setX(u.getX() + 1);
                        break;
                    default:
                        break;
                }
            }
        }
        return this;
    }

    @Override
    public String toString() {
        String result = "";
        if(parentAction == null) {
            result += "F[";
            for(ABUnit u: footmen) result += u + " ";
            result += "] A[";
            for(ABUnit u: archers) result += u + " ";
            result += "]";
        } else {
            result += "[" + eval() + "|";
            for(Map.Entry<Integer, ABAction> e: parentAction.entrySet())
                result += e.getKey() + ":" + e.getValue() + " ";
            result += "]";
        }
        return result;
    }

    // evaluation heuristic
    // target the first archer and try to corner him
    public int eval() {
        if(score == null) {
            score = 0;
            ABUnit a = archers.getFirst();
            ABUnit f1 = footmen.getFirst();
            ABUnit f2 = footmen.getLast();
            int dx1 = Math.abs(a.getX() - f1.getX());
            int dy1 = Math.abs(a.getY() - f1.getY());
            int dx2 = Math.abs(a.getX() - f2.getX());
            int dy2 = Math.abs(a.getY() - f2.getY());
            score -= dx1 * 10 + dy1 + dx2 + dy2 * 10;
        }
        return score;
    }

    public LinkedList<ABUnit> getFootmen() { return footmen; }

    public LinkedList<ABUnit> getArchers() { return archers; }

    public HashMap<Integer, ABAction> getParentAction() { return parentAction; }

    public boolean isTerminal() { return (footmen.isEmpty() || archers.isEmpty()); }
}
