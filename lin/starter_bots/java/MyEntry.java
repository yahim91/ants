
import java.util.Map.Entry;
import java.lang.Object;



/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author mihai
 */
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
     return (((i.intValue()              
             + Math.abs(k.col() - k.direction.col()))
             + Math.abs(k.row() - k.direction.row()))
             - (t.getValue().intValue() 
             + Math.abs(t.getKey().col() - t.getKey().direction.col())
             + Math.abs(t.getKey().row() - t.getKey().direction.row())));
    }
          
}
