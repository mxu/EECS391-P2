import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.util.Direction;
import edu.cwru.sepia.util.DistanceMetrics;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by Mike on 2/15/14.
 * This agent instructs 2 footmen to attack 1 or 2 archers using Alpha-Beta Pruning
 */
public class ABAgent extends Agent {

    private int step;
    private int xMax;
    private int yMax;
    private int numPlys;

    private HashMap<ABAction, Direction> directions;

    public ABAgent(int playernum, String[] args) {
        super(playernum);
        directions = new HashMap<ABAction, Direction>();
        directions.put(ABAction.MOVE_UP, Direction.NORTH);
        directions.put(ABAction.MOVE_DOWN, Direction.SOUTH);
        directions.put(ABAction.MOVE_LEFT, Direction.WEST);
        directions.put(ABAction.MOVE_RIGHT, Direction.EAST);
        numPlys = Integer.parseInt(args[0]);
        System.out.println("ABAgent initialized with numPlys = " + numPlys);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newState, History.HistoryView stateHistory) {
        step = 0;
        xMax = newState.getXExtent();
        yMax = newState.getYExtent();
        return middleStep(newState, stateHistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newState, History.HistoryView stateHistory) {
        step++;
        System.out.println("=> Step: " + step);
        ABState abState = new ABState(newState);
        // execute action returned by alpha-beta search
        return translateActions(alphaBeta(abState, 0, true, new ABState(Integer.MIN_VALUE), new ABState(Integer.MAX_VALUE)).getParentAction(), abState);
    }

    // heuristic minimax search with alpha-beta pruning
    private ABState alphaBeta(ABState s, int ply, boolean isMax, ABState alpha, ABState beta) {
        // cutoff on max ply or terminal node
        if(ply == numPlys || s.isTerminal()) return s;

        // generate children states from possible actions
        List<ABState> children = new ArrayList<ABState>();
        for(HashMap<Integer, ABAction> action: getActions(s, isMax)) {
            // create new state by applying actions
            ABState ns = new ABState(s, action, isMax);
            boolean added = false;
            // insert higher scores in front
            for(int i = 0; i < children.size(); i++) {
                if(ns.eval() > children.get(i).eval()) {
                    children.add(i, ns);
                    added = true;
                    break;
                }
            }
            if(!added) children.add(ns);
        }

        System.out.println(tabs(ply) + "EXPANDING " + s + ": " + fmtAB(isMax, alpha, beta));
        // recurse on each child node
        for(ABState c: children) {
            int v = alphaBeta(c, ply + 1, !isMax, alpha, beta).eval();
            if(isMax && v > alpha.eval()) {
                System.out.println(tabs(ply) + "\u03B1-> " + c + ": " + fmtA(alpha) + " > " + fmtA(c));
                alpha = c;
            } else if(!isMax && v < beta.eval()) {
                System.out.println(tabs(ply) + "\u03B2-> " + c + ": " + fmtB(beta) + " > " + fmtB(c));
                beta = c;
            }
            if(alpha.eval() >= beta.eval()) {
                System.out.println(tabs(ply) + "\\-> " + c + ": " + fmtA(alpha) + " >= " + fmtB(beta));
                return c;
            }
        }
        System.out.println(tabs(ply) + "-->" + (isMax ? alpha : beta));
        return isMax ? alpha : beta;
    }

    // get all possible combinations of actions for given state
    private List<HashMap<Integer, ABAction>> getActions(ABState s, boolean isMax) {
        ABUnit e1 = isMax ? s.getArchers().getFirst() : s.getFootmen().getFirst();
        ABUnit e2 = isMax ? s.getArchers().getLast() : s.getFootmen().getLast();
        ABUnit u1 = isMax ? s.getFootmen().getFirst() : s.getArchers().getFirst();
        List<ABAction> u1Actions = getUnitActions(u1, e1, e2, isMax ? 1 : 12);
        List<HashMap<Integer, ABAction>> result = new LinkedList<HashMap<Integer, ABAction>>();

        // if there is only one unit, list its actions
        for(ABAction u1a: u1Actions) {
            HashMap<Integer, ABAction> am = new HashMap<Integer, ABAction>();
            am.put(u1.getId(), u1a);
            result.add(am);
        }

        // if there is a second unit...
        if(isMax ? s.getFootmen().size() == 2 : s.getArchers().size() == 2) {
            ABUnit u2 = isMax ? s.getFootmen().getLast() : s.getArchers().getLast();
            List<ABAction> u2Actions = getUnitActions(u2, e1, e2, isMax ? 1 : 12);
            List<HashMap<Integer, ABAction>> temp = new LinkedList<HashMap<Integer, ABAction>>();
            // combine its actions with the first unit
            for(ABAction u2a: u2Actions) {
                for(HashMap<Integer, ABAction> am: result) {
                    am.put(u2.getId(), u2a);
                    temp.add(new HashMap<Integer, ABAction>(am));
                }
            }
            result = temp;
        }
        return result;
    }

    // get all possible actions for unit
    private List<ABAction> getUnitActions(ABUnit unit, ABUnit enemy1, ABUnit enemy2, int atkRange) {
        List<ABAction> result = new ArrayList<ABAction>();
        boolean twoEnemies = !enemy1.equals(enemy2);
        int dx1 = enemy1.getX() - unit.getX();
        int dy1 = enemy1.getY() - unit.getY();
        int dx2 = twoEnemies ? enemy2.getX() - unit.getX() : dx1;
        int dy2 = twoEnemies ? enemy2.getY() - unit.getY() : dy1;
        // test border, if enemy 1 is blocking, and if enemy 2 is blocking
        if(unit.getY() > 0 && !(dx1 == 0 && dy1 == -1) && (!twoEnemies || !(dx2 == 0 && dy2 == -1)))
            result.add(ABAction.MOVE_UP);
        if(unit.getY() < yMax && !(dx1 == 0 && dy1 == 1) && (!twoEnemies || !(dx2 == 0 && dy2 == 1)))
            result.add(ABAction.MOVE_DOWN);
        if(unit.getX() > 0 && !(dx1 == -1 && dy1 == 0) && (!twoEnemies || !(dx2 == -1 && dy2 == 0)))
            result.add(ABAction.MOVE_LEFT);
        if(unit.getX() < xMax && !(dx1 == 1 && dy1 == 0) && (!twoEnemies || !(dx2 == 1 && dy2 == 0)))
            result.add(ABAction.MOVE_RIGHT);
        // test if unit is in range to attack
        if(((Math.abs(dx1) + Math.abs(dy1)) <= atkRange) || (twoEnemies && ((Math.abs(dx2) + Math.abs(dy2)) <= atkRange)))
            result.add(ABAction.ATTACK);
        return result;
    }

    @Override
    public void terminalStep(State.StateView newState, History.HistoryView stateHistory) {
        step++;
        System.out.println("Task completed in " + step + "steps");
    }

    @Override
    public void savePlayerData(OutputStream outputStream) {}

    @Override
    public void loadPlayerData(InputStream inputStream) {}

    // translate ABActions into Actions
    private HashMap<Integer, Action> translateActions(HashMap<Integer, ABAction> abActions, ABState state) {
        HashMap<Integer, Action> actions = new HashMap<Integer, Action>();
        for(Map.Entry<Integer, ABAction> e: abActions.entrySet()) {
            actions.put(e.getKey(), (e.getValue() == ABAction.ATTACK) ?
                    Action.createCompoundAttack(e.getKey(), getTargetId(state, e.getKey())) :
                    Action.createPrimitiveMove(e.getKey(), directions.get(e.getValue())));
        }
        return actions;
    }

    // get id of nearest archer in range of footman
    private int getTargetId(ABState state, int footmanId) {
        for(ABUnit footman: state.getFootmen())
            if(footman.getId() == footmanId)
                for(ABUnit archer: state.getArchers())
                    if(DistanceMetrics.chebyshevDistance(archer.getX(), archer.getY(), footman.getX(), footman.getY()) == 1)
                        return archer.getId();
        return -1;
    }

    // helper functions for formatting debug statements
    private String fmtAB(boolean isMax, ABState a, ABState b) {
        String s = (isMax ? "MAX" : "MIN");
        s += " " + fmtA(a);
        s += " " + fmtB(b);
        return s;
    }

    private String fmtA(ABState a) {
        return "\u03B1:" + (a.eval() == Integer.MIN_VALUE ? "-\u221E" : a.eval());
    }

    private String fmtB(ABState b) {
        return "\u03B2:" + (b.eval() == Integer.MAX_VALUE ? "+\u221E" : b.eval());
    }

    private String tabs(int n) {
        String s = "";
        while(n-- > 0) s += "\t";
        return s;
    }
}
