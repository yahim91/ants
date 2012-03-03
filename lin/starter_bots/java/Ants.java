
import java.util.Map.Entry;
import java.util.*;

public class Ants {

    private int turn = 0;
    private int turns = 0;
    private static int rows = 0;
    private static int cols = 0;
    private int loadtime = 0;
    private int turntime = 0;
    private int viewradius2 = 0;
    private int attackradius2 = 0;
    private int spawnradius2 = 0;
    private long seed = 0;
    private Ilk map[][];
    private Map<Tile, Ilk> antList = new HashMap<Tile, Ilk>();
    private Set<Tile> foodList = new HashSet<Tile>();
    private Set<Tile> deadList = new HashSet<Tile>();
    private Set<Tile> visionOffs = new HashSet<Tile>();
    private boolean[][] visible;
    public int[][] time;
    public HashMap<Tile, Tile> foodTargets;
    private Set<Tile> myHills;
    private Set<Tile> enemyHills;
    public Map<Integer, HashSet<Tile>> intToArea;
    public int[][] id;
    public Tile source[][];
    public HashMap<Tile, Tile> missions;

    public int turn() {
        return this.turn;
    }

    public int turns() {
        return this.turns;
    }

    public static int rows() {
        return Ants.rows;
    }

    public static int cols() {
        return Ants.cols;
    }

    public int loadtime() {
        return this.loadtime;
    }

    public int turntime() {
        return this.turntime;
    }

    public int viewradius2() {
        return this.viewradius2;
    }

    public int attackradius2() {
        return this.attackradius2;
    }

    public int spawnradius2() {
        return this.spawnradius2;
    }

    public long seed() {
        return this.seed;
    }

    public boolean isVisible(Tile tile) {
        return visible[tile.row()][tile.col()];
    }

    public boolean setup(List<String> data) {
        try {
            for (String line : data) {
                String tokens[] = line.toLowerCase().split(" ");
                if (tokens[0].equals("cols")) {
                    this.cols = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("rows")) {
                    this.rows = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("turns")) {
                    this.turns = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("loadtime")) {
                    this.loadtime = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("turntime")) {
                    this.turntime = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("viewradius2")) {
                    this.viewradius2 = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("attackradius2")) {
                    this.attackradius2 = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("spawnradius2")) {
                    this.spawnradius2 = Integer.parseInt(tokens[1]);
                } else if (tokens[0].equals("player_seed")) {
                    this.seed = Long.parseLong(tokens[1]);
                }
            }
            this.time = new int[this.rows][this.cols];
            this.visible = new boolean[this.rows][this.cols];
            this.myHills = new HashSet<Tile>();
            this.enemyHills = new HashSet<Tile>();
            this.missions = new HashMap<Tile, Tile>();

            for (int i = 0; i < this.rows; ++i) {
                Arrays.fill(visible[i], false);
            }
            this.map = new Ilk[this.rows][this.cols];
            for (Ilk[] row : this.map) {
                Arrays.fill(row, Ilk.LAND);
            }
            //calc vision offsets
            int m = (int) Math.sqrt(viewradius2);
            for (int r = -m; r <= m; ++r) {
                for (int c = -m; c <= m; ++c) {
                    int dist = r * r + c * c;
                    if (dist <= viewradius2) {
                        visionOffs.add(new Tile(r, c));
                    }
                }
            }

            // setare timp spatiu neexplorat
            for (int i = 0; i <= rows; i++) {
                Arrays.fill(time[i], 1000);
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean update(List<String> data) {
        // clear ants and food
        for (Tile ant : this.antList.keySet()) {
            this.map[ant.row()][ant.col()] = Ilk.LAND;
        }
        this.antList.clear();
        for (Tile food : this.foodList) {
            this.map[food.row()][food.col()] = Ilk.LAND;
        }
        this.foodList.clear();
        for (Tile dead : this.deadList) {
            this.map[dead.row()][dead.col()] = Ilk.LAND;
        }
        this.deadList.clear();

        // clear visible
        for (int i = 0; i < this.rows; ++i) {
            Arrays.fill(visible[i], false);
        }

        // get new tile ilks
        for (String line : data) {
            String tokens[] = line.split(" ");
            if (tokens.length > 2) {
                int row = Integer.parseInt(tokens[1]);
                int col = Integer.parseInt(tokens[2]);
                if (tokens[0].equals("w")) {
                    this.map[row][col] = Ilk.WATER;
                    this.time[row][col] = 0;
                } else if (tokens[0].equals("a")) {
                    Ilk ilk = Ilk.fromId(Integer.parseInt(tokens[3]));
                    this.map[row][col] = ilk;
                    this.antList.put(new Tile(row, col), ilk);
                } else if (tokens[0].equals("f")) {
                    this.map[row][col] = Ilk.FOOD;
                    this.foodList.add(new Tile(row, col));
                } else if (tokens[0].equals("d")) {
                    this.map[row][col] = Ilk.DEAD;
                    this.deadList.add(new Tile(row, col));
                } else if (tokens[0].equals("h")) {
                    if (Integer.parseInt(tokens[3]) <= 0) {
                        myHills.add(new Tile(row, col));
                    } else {
                        enemyHills.add(new Tile(row, col));
                    }

                }
            }
        }

        // incrementare timp camp neexplorat
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                time[i][j]++;
            }
        }

        // get vision tiles
        for (Tile ant : myAnts()) {
            for (Tile offset : visionOffs) {
                Tile temp = tile(ant, offset);
                visible[temp.row()][temp.col()] = true;
                time[temp.row()][temp.col()] = 0;
            }
        }
        this.id = new int[this.rows][this.cols];
        this.source = new Tile[this.rows][this.cols];
        return true;
    }

    /**
     * !!!! FOLOSIRE DOAR DACA T2 DIFERA DE T1
     *
     * @param t1
     * @param t2
     */
    public void issueOrder(Tile t1, Tile t2) {
        Aim direction = null;
        if (t1.row() < t2.row()) {
            if (t2.row() - t1.row() >= this.rows / 2) {
                direction = Aim.NORTH;
            } else {
                direction = Aim.SOUTH;
            }
        } else if (t1.row() > t2.row()) {
            if (t1.row() - t2.row() >= this.rows / 2) {
                direction = Aim.SOUTH;
            } else {
                direction = Aim.NORTH;
            }
        }

        if (t1.col() < t2.col()) {
            if (t2.col() - t1.col() >= this.cols / 2) {
                direction = Aim.WEST;
            } else {
                direction = Aim.EAST;
            }
        } else if (t1.col() > t2.col()) {
            if (t1.col() - t2.col() >= this.cols / 2) {
                direction = Aim.EAST;
            } else {
                direction = Aim.WEST;
            }
        }

        System.out.println("o " + t1.row() + " " + t1.col() + " " + direction.symbol);
        System.out.flush();
    }

    public Tile getSource(Tile t) {
        return source[t.row()][t.col()];
    }

    public int getId(Tile t) {
        return id[t.row()][t.col()];
    }

    public Set<Tile> myHills() {
        return this.myHills;
    }

    public Set<Tile> enemyHills() {
        return this.enemyHills;
    }

    public void issueOrder(int row, int col, Aim direction) {
        System.out.println("o " + row + " " + col + " " + direction.symbol);
        System.out.flush();
    }

    public void issueOrder(Tile ant, Aim direction) {
        System.out.println("o " + ant.row() + " " + ant.col() + " " + direction.symbol);
        System.out.flush();
    }

    public void finishTurn() {
        System.out.println("go");
        System.out.flush();
        this.turn++;
    }

    public Set<Tile> myAnts() {
        Set<Tile> myAnts = new HashSet<Tile>();
        for (Entry<Tile, Ilk> ant : this.antList.entrySet()) {
            if (ant.getValue() == Ilk.MY_ANT) {
                myAnts.add(ant.getKey());
            }
        }
        return myAnts;
    }

    public Set<Tile> enemyAnts() {
        Set<Tile> enemyAnts = new HashSet<Tile>();
        for (Entry<Tile, Ilk> ant : this.antList.entrySet()) {
            if (ant.getValue().isEnemy()) {
                enemyAnts.add(ant.getKey());
            }
        }
        return enemyAnts;
    }

    public Set<Tile> food() {
        return new HashSet<Tile>(this.foodList);
    }

    public int distance(Tile t1, Tile t2) {
        int dRow = Math.abs(t1.row() - t2.row());
        int dCol = Math.abs(t1.col() - t2.col());

        dRow = Math.min(dRow, this.rows - dRow);
        dCol = Math.min(dCol, this.cols - dCol);

        return dRow * dRow + dCol * dCol;
    }

    public List<Aim> directions(Tile t1, Tile t2) {
        List<Aim> directions = new ArrayList<Aim>();

        if (t1.row() < t2.row()) {
            if (t2.row() - t1.row() >= this.rows / 2) {
                directions.add(Aim.NORTH);
            } else {
                directions.add(Aim.SOUTH);
            }
        } else if (t1.row() > t2.row()) {
            if (t1.row() - t2.row() >= this.rows / 2) {
                directions.add(Aim.SOUTH);
            } else {
                directions.add(Aim.NORTH);
            }
        }

        if (t1.col() < t2.col()) {
            if (t2.col() - t1.col() >= this.cols / 2) {
                directions.add(Aim.WEST);
            } else {
                directions.add(Aim.EAST);
            }
        } else if (t1.col() > t2.col()) {
            if (t1.col() - t2.col() >= this.cols / 2) {
                directions.add(Aim.EAST);
            } else {
                directions.add(Aim.WEST);
            }
        }

        return directions;
    }

    /**
     * intoarce timpul de cand tile-ul nu a mai fost vizitat
     *
     * @param tile
     * @return int
     */
    public int time(Tile tile) {
        return time[tile.row()][tile.col()];
    }

    public Ilk ilk(Tile location, Aim direction) {
        Tile new_location = this.tile(location, direction);
        return this.map[new_location.row()][new_location.col()];
    }

    public Ilk ilk(Tile location) {
        return this.map[location.row()][location.col()];
    }

    /**
     *
     * @param tile
     * @param offset
     * @return new Tile
     */
    public Tile tile(Tile tile, Tile offset) {
        int row = (tile.row() + offset.row()) % rows;
        if (row < 0) {
            row += rows;
        }
        int col = (tile.col() + offset.col()) % cols;
        if (col < 0) {
            col += cols;
        }
        return new Tile(row, col);
    }

    public Tile tile(Tile location, Aim direction) {
        int nRow = (location.row() + direction.dRow) % this.rows;
        if (nRow < 0) {
            nRow += this.rows;
        }
        int nCol = (location.col() + direction.dCol) % this.cols;
        if (nCol < 0) {
            nCol += this.cols;
        }
        return new Tile(nRow, nCol);
    }

    public static void run(Bot bot) {
        Ants ants = new Ants();
        StringBuffer line = new StringBuffer();
        ArrayList<String> data = new ArrayList<String>();
        int c;
        try {
            while ((c = System.in.read()) >= 0) {
                switch (c) {
                    case '\n':
                    case '\r':
                        if (line.length() > 0) {
                            String full_line = line.toString();
                            if (full_line.equals("ready")) {
                                ants.setup(data);
                                ants.finishTurn();
                                data.clear();
                            } else if (full_line.equals("go")) {
                                ants.update(data);
                                bot.do_turn(ants);
                                ants.finishTurn();
                                data.clear();
                            } else {
                                if (line.length() > 0) {
                                    data.add(full_line);
                                }
                            }
                            line = new StringBuffer();
                        }
                        break;
                    default:
                        line.append((char) c);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void gatherFood() {
        Set<Tile> visited = new HashSet<Tile>();
        LinkedList<Tile> toBeProcessed = new LinkedList<Tile>();
        for (Tile food : foodList) {
            food.dist = 0;
            food.source = food;
            food.assigned = false;
            visited.add(food);
            toBeProcessed.addFirst(food);
        }

        int foodAmount = visited.size();
        foodTargets = new HashMap<Tile, Tile>();

        while (!toBeProcessed.isEmpty()) {
            Tile temp = toBeProcessed.removeLast();
            if (foodTargets.size() == foodAmount) {
                break;
            }
            if (temp.dist <= 12 || !temp.source.assigned) {
                for (Aim aim : Aim.values()) {
                    Tile next = tile(temp, aim);
                    if (!visited.contains(next)
                            && ilk(next).isPassable()
                            && isVisible(next) && !temp.source.assigned) {

                        if (ilk(temp, aim) == Ilk.MY_ANT) {
                            foodTargets.put(next, temp);
                            temp.source.assigned = true;

                        }

                        next.dist = temp.dist + 1;
                        next.source = temp.source;
                        next.ancestor = temp;
                        visited.add(next);
                        toBeProcessed.addFirst(next);
                    }
                }
            }
        }
    }

    public void createMyAreas() {
        intToArea = new HashMap<Integer, HashSet<Tile>>();
        LinkedList<Tile> toBeProcessed = new LinkedList<Tile>();
        HashSet<Tile> visited = new HashSet<Tile>();
        Set<Tile> sources = new HashSet<Tile>();
        int _id = 1;
        for (Tile myAnt : myAnts()) {
            id[myAnt.row()][myAnt.col()] = _id;
            source[myAnt.row()][myAnt.col()] = myAnt;
            HashSet<Tile> area = new HashSet<Tile>();
            area.add(myAnt);
            intToArea.put(_id, area);
            myAnt.dist = 0;
            visited.add(myAnt);
            toBeProcessed.addFirst(myAnt);
            sources.add(myAnt);
            _id++;
        }

        HashSet<Tile> enemyArea = new HashSet<Tile>();
        for (Tile enemyAnt : enemyAnts()) {
            source[enemyAnt.row()][enemyAnt.col()] = enemyAnt;
            enemyAnt.dist = 0;
            enemyArea.add(enemyAnt);
            visited.add(enemyAnt);
            toBeProcessed.addFirst(enemyAnt);
        }
        intToArea.put(0, enemyArea);
        boolean toBeRemoved;

        while (!toBeProcessed.isEmpty()) {
            Tile curr;
            curr = toBeProcessed.removeLast();
            toBeRemoved = true;
            HashSet<Tile> currArea = intToArea.get(getId(getSource(curr)));

            for (Aim aim : Aim.values()) {
                Tile next = tile(curr, aim);
                if (ilk(next).isPassable()
                        && (!visited.contains(next)
                        || (visited.contains(next)
                        && getId(getSource(next)) != getId(getSource(curr))))
                        && curr.dist < 20) {
                    if (!visited.contains(next)) {
                        source[next.row()][next.col()] = getSource(curr);
                        next.dist = curr.dist + 1;
                        currArea.add(next);
                        visited.add(next);
                        toBeProcessed.addFirst(next);
                    } else {
                        if (getId(getSource(next)) != 0 && getId(getSource(curr)) != 0
                                && getId(getSource(next)) != getId(getSource(curr))) {
                            HashSet<Tile> nextArea = intToArea.get(getId(getSource(next)));
                            if (!nextArea.equals(currArea)) {
                                currArea.addAll(nextArea);
                                intToArea.remove(getId(getSource(next)));
                                int idNext = getId(getSource(next));
                                for (Tile _source : sources) {
                                    if (getId(_source) == idNext) {
                                        id[_source.row()][_source.col()] = getId(getSource(curr));
                                    }
                                }

                            }
                        }
                    }
                }
                if (ilk(next) == Ilk.WATER || curr.dist == 20 || getId(getSource(next)) == 0) {
                    toBeRemoved = false;
                }


            }

            if (toBeRemoved) {
                currArea.remove(curr);
            }
            intToArea.put(getId(getSource(curr)), currArea);
        }
    }
}
