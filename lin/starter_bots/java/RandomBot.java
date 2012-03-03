
import java.util.*;

public class RandomBot implements Bot {

    public static void main(String[] args) {
        Ants.run(new RandomBot());
    }

    public Aim aStar(Tile source, Tile dest, Set<Tile> destinations, Ants ants) {

        PriorityQueue<Map.Entry<Tile, Integer>> toBeProcessed = new PriorityQueue<Map.Entry<Tile, Integer>>();
        HashSet<Tile> visited = new HashSet<Tile>();
        toBeProcessed.add(new MyEntry(source, 0));
        visited.add(source);
        source.direction = dest;

        while (!toBeProcessed.isEmpty()) {
            MyEntry currEntry = (MyEntry) toBeProcessed.poll();
            for (Aim aim : Aim.values()) {
                Tile next = ants.tile(currEntry.getKey(), aim);
                if (currEntry.getKey().equals(source)) {
                    next._source = aim;
                } else {
                    next._source = currEntry.getKey()._source;
                }
                next.direction = dest;
                if (!visited.contains(next) && ants.ilk(next).isPassable()) {
                    toBeProcessed.add(new MyEntry(next, currEntry.getValue() + 1));
                    visited.add(next);
                    if (next.equals(dest)) {
                        if (!destinations.contains(next)) {
                            return next._source;
                        } else {
                            return null;
                        }
                    }

                }
            }
        }
        return null;
    }

    /**
     * metoda explore pentru o furnica fara task
     *
     * @param antLoc
     * @param ants
     * @return
     */
    public Aim explore(Tile antLoc, Ants ants, Set<Tile> destinations) {

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
            if (temp.dist <= 13) {



                for (Aim aim : Aim.values()) {
                    if (!visited.contains(ants.tile(temp, aim)) && ants.ilk(temp, aim).isPassable()) {
                        Tile n = ants.tile(temp, aim);
                        n.dist = temp.dist + 1;
                        n.direction = temp.direction;
                        ants.time[n.direction.row()][n.direction.col()] += ants.time(n);
                        visited.add(n);
                        toVisit.addFirst(n);
                    }
                }
            }
        }

        // determinare directie cu suma timpului cel mai mare
        int max = 0;
        for (Aim aim : Aim.values()) {
            int timeTemp = ants.time(ants.tile(antLoc, aim));
            Tile t = ants.tile(antLoc, aim);
            if (timeTemp > max && !destinations.contains(t)) {
                max = timeTemp;
                next = aim;
            }
            ants.time[t.row()][t.col()] = 0;
        }
        return next;
    }

    public void do_turn(Ants ants) {
        Set<Tile> destinations = new HashSet<Tile>();

        for (Tile myHill : ants.myHills()) {
            destinations.add(myHill);
        }
        ants.createMyAreas();
        ants.gatherFood();

        for (Tile antLoc : ants.myAnts()) {
            boolean issued = false;
            if (ants.foodTargets.containsKey(antLoc)
                    && !destinations.contains(ants.foodTargets.get(antLoc))) {
                destinations.add(ants.foodTargets.get(antLoc));
                ants.issueOrder(antLoc, ants.foodTargets.get(antLoc));
                if (ants.missions.containsKey(antLoc)) {
                    ants.missions.remove(antLoc);
                }
                issued = true;
            } else if (!ants.missions.containsKey(antLoc)) {
                Aim next = explore(antLoc, ants, destinations);
                if (next != null) {
                    ants.issueOrder(antLoc, next);
                    destinations.add(ants.tile(antLoc, next));
                    issued = true;
                }
            }
            if (issued == false) {
                if (!ants.missions.containsKey(antLoc)) {
                    Object[] border = ants.intToArea.get(ants.getId(antLoc)).toArray();
                    int min = ((Tile)border[0]).manhattanDist(antLoc);
                    int minp = 0;
                    for (int i = 1; i < border.length; i++) {
                        if (min > ((Tile)border[i]).manhattanDist(antLoc)) {
                            min = ((Tile)border[0]).manhattanDist(antLoc);
                            minp = i;
                        }
                    }
                    ants.missions.put(antLoc, (Tile) border[minp]);
                }
                if (!ants.missions.get(antLoc).equals(antLoc)) {
                    Aim _next = aStar(antLoc, ants.missions.get(antLoc), destinations, ants);
                    if (_next != null && !destinations.contains(ants.tile(antLoc, _next)) 
                            && ants.ilk(antLoc, _next).isUnoccupied()) {
                        destinations.add(ants.tile(antLoc, _next));
                        Tile dest = ants.missions.get(antLoc);
                        ants.missions.remove(antLoc);
                        if (_next != null) {
                            issued = true;
                            ants.issueOrder(antLoc, _next);
                            ants.missions.put(ants.tile(antLoc, _next), dest);
                        }
                    }
                } else {
                    ants.missions.remove(antLoc);
                }
                

            }
            if (issued == false) {
 //               if (destinations.contains(antLoc)) {
                    for (Aim aim : Aim.values()) {
                        if (!destinations.contains(ants.tile(antLoc, aim))
                                && ants.ilk(antLoc, aim).isUnoccupied()) {
                            ants.issueOrder(antLoc, aim);
                            destinations.add(ants.tile(antLoc, aim));
                            issued = true;
                            break;
                        }
                    }
 //               }
            }
            if (issued == false ) {
                  destinations.add(antLoc);
            }
        }
    }
}
