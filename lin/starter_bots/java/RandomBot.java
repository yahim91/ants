
import java.util.*;

public class RandomBot implements Bot {

    public static void main(String[] args) {
        Ants.run(new RandomBot());
    }
    

    /**
     * metoda explore pentru o furnica fara task
     *
     * @param antLoc
     * @param ants
     * @return
     */
    public Aim explore(Tile antLoc, Ants ants) {

        Aim next = null;
        LinkedList<Tile> toVisit = new LinkedList<Tile>();
        Set<Tile> visited = new HashSet<Tile>();
        antLoc.dist = 0;
        for (Aim aim : Aim.values()) {
            if (ants.ilk(antLoc, aim).isUnoccupied()) {
                Tile n = ants.tile(antLoc, aim);
                n.dist = antLoc.dist + 1;
                visited.add(n);
                toVisit.addFirst(n);
                n.direction = n;
            }
        }

        visited.add(antLoc);
        while (!toVisit.isEmpty()) {
            Tile temp = toVisit.removeLast();
            if (temp.dist > 10) {
                break;
            }

            for (Aim aim : Aim.values()) {
                if (!visited.contains(ants.tile(temp, aim)) && ants.ilk(temp, aim).isUnoccupied()) {
                    Tile n = ants.tile(temp, aim);
                    n.dist = temp.dist + 1;
                    n.direction = temp.direction;
                    ants.time[n.direction.row()][n.direction.col()] += ants.time(n);
                    visited.add(n);
                    toVisit.addFirst(n);
                }
            }
        }

        // determinare directie cu suma timpului cel mai mare
        int max = 0;
        for (Aim aim : Aim.values()) {
            int timeTemp = ants.time(ants.tile(antLoc, aim));
            if (timeTemp > max) {
                max = timeTemp;
                next = aim;
            }
        }

        return next;
    }

    public void do_turn(Ants ants) {
        Set<Tile> destinations = new HashSet<Tile>();
        for (Tile antLoc : ants.myAnts()) {
            boolean issued = false;
            Aim next = explore(antLoc, ants);
            if (next != null) {
                ants.issueOrder(antLoc, next);
                destinations.add(ants.tile(antLoc, next));
                issued = true;
            }

            if (!issued) {
                destinations.add(antLoc);
            }
        }
    }
}
