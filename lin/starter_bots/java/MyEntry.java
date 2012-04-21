
import java.util.Map.Entry;
import java.lang.Object;




public class MyEntry implements Entry<Tile, Integer>, Comparable<MyEntry> {

    private Tile k;
    private Integer i;
    
    MyEntry(Tile source, Integer i) {
        this.k = source;
        this.i = i;
        
    }

    @Override
    public Tile getKey() {
        return this.k;
    }

    @Override
    public Integer getValue() {
        return this.i;
    }

    @Override
    public Integer setValue(Integer v) {
        return this.i = v;
    }

    @Override
    public int compareTo(MyEntry t) {
     return ((i.intValue()              
             + k.manhattanDist(k.direction))
             - (t.getValue().intValue() 
             + t.getKey().manhattanDist(t.getKey().direction)));
    }
          
}
