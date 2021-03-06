
import java.util.*;
import java.util.Map.Entry;

public class RandomBot implements Bot {

    public static void main(String[] args) {
        Ants.run(new RandomBot());
    }

    public Entry assignMission(Tile antLoc, Ants ants, Set<Tile> borders, Set<Tile> destinations) {
        HashSet<Tile> visited = new HashSet<Tile>();
        LinkedList<Tile> toBeProcessed = new LinkedList<Tile>();
        antLoc.dist = 0;
        visited.add(antLoc);
        toBeProcessed.addFirst(antLoc);
        HashMap<Tile, Aim> res = new HashMap<Tile, Aim>();
        while (!toBeProcessed.isEmpty()) {
            Tile curr = toBeProcessed.removeLast();
            if (borders.contains(curr)) {
                res.put(curr, curr._source);
                Iterator it = res.entrySet().iterator();
                Entry result = (Entry) it.next();
                return result;
            }
            if (curr.dist <= 100) {
                for (Aim aim : Aim.values()) {
                    Tile next = ants.tile(curr, aim);
                    if (!visited.contains(next) && ants.ilk(next).isPassable()
                            && ((curr.equals(antLoc) && !destinations.contains(next)
                            && ants.ilk(next).isUnoccupied()) || !curr.equals(antLoc))) {
                        if (curr.equals(antLoc)) {
                            next._source = aim;
                        } else {
                            next._source = curr._source;
                        }
                        next.dist = curr.dist + 1;
                        visited.add(next);
                        toBeProcessed.addFirst(next);
                    }
                }
            }
        }
        return null;
    }

    /*
     Metoda ce realizeaza A* intre 2 tile-uri
     */
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
                if (!visited.contains(next) && ants.ilk(next).isPassable()
                        && ((currEntry.getKey().equals(source) && !destinations.contains(next)) || !currEntry.getKey().equals(source))) {
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

    /*
     metoda explore prin BFS pentru o furnica fara task
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
                    Tile n = ants.tile(temp, aim);
                    if (!visited.contains(n) && ants.ilk(n).isPassable()
                            && ((temp.equals(antLoc) && !destinations.contains(n)) || !temp.equals(n))) {
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
        Set<Tile> issues = new HashSet<Tile> ();
        ArrayList < ArrayList <Pair<Tile, Aim>>> battleAnts =
                new ArrayList < ArrayList <Pair<Tile, Aim>>>(ants.battle(ants.battleAreas()));
        
        /*for (Tile myHill : ants.myHills()) {
            destinations.add(myHill);
        }*/
        
        for (ArrayList <Pair<Tile, Aim>> x : battleAnts){
            for (Pair<Tile, Aim> y : x){
                Tile next;
                if (y.snd == null) {
                    next = y.fst;
                } else {
                    next = ants.tile (y.fst, y.snd);
                }
                if (!destinations.contains(next) && ants.ilk(next).isPassable())
                {
                    ants.issueOrder(y.fst, next);
                    destinations.add(next);
                    issues.add(y.fst);
                }
            }
        }
        
        ants.createMyAreas();
        ants.gatherFood();
        //HashSet<Tile> myAnts = new HashSet<Tile> (ants.myAnts());
        
        //myAnts.removeAll(issues);
        HashSet<Tile> myAntss = (HashSet<Tile>) ((HashSet<Tile>)ants.myAnts()).clone();
        myAntss.removeAll(issues);
        for (Tile antLoc : myAntss) {
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
                   
                    Entry mission = assignMission(antLoc, ants, ants.intToArea.get(ants.getId(antLoc)), destinations);
                    if (mission != null
                            && !destinations.contains(ants.tile(antLoc, (Aim) mission.getValue()))) {
                        Aim _aim = (Aim) mission.getValue();
                        Tile _dest = (Tile) mission.getKey();
                        ants.missions.put(ants.tile(antLoc, _aim), _dest);
                        destinations.add(ants.tile(antLoc, _aim));
                        ants.issueOrder(antLoc, _aim);
                        issued = true;
                    } else {
                        Random r = new Random(ants.seed());
                        Object[] border = ants.intToArea.get(ants.getId(antLoc)).toArray();
                        ants.missions.put(antLoc, (Tile) border[r.nextInt(border.length)]);
                    }
                }
                if (issued == false) {
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
            if (issued == false) {
                destinations.add(antLoc);
            }
        }
    }
}
