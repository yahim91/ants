
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tile {
	Tile(int row, int col) {
		this.row = row;
		this.col = col;
	}

	private int row;
	private int col;
        public int dist;
        public Tile direction;
        public Tile ancestor;
        public Tile source;
        public boolean assigned;
        public int id;
	
	public int row() {
		return this.row;
	}
	
	public int col() {
		return this.col;
	}
	
	public int hashCode() {
		return this.row * 65536 + this.col;
	}
	
	public boolean equals(Object o) {
		if (o.getClass() == Tile.class) {
			return this.row == ((Tile)o).row() && this.col == ((Tile)o).col();
		} else {
			return false;
		}
	}
	
	public String toString() {
		return "(" + this.row + "," + this.col + ")";
	}
}
