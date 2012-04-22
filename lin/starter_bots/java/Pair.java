/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mihai
 */
public class Pair<A, B> {

    public A fst;
    public B snd;

    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    Pair() {
        
    }

    public A getFirst() {
        return fst;
    }

    public B getSecond() {
        return snd;
    }

    public void setFirst(A v) {
        this.fst = v;
    }

    public void setSecond(B v) {
        this.snd = v;
    }
}
