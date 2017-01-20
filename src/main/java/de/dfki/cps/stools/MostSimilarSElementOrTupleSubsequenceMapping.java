/*
 * Copyright (c) Serge Autexier, Dominik Dietrich, DFKI GmbH 2011.
 */

package de.dfki.cps.stools;

import de.dfki.cps.stools.SElement;
import de.dfki.cps.stools.SToolInterface;
import de.dfki.cps.utils.MostSimilarSubsequenceMapping;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: autexier
 * Date: 14.02.2011
 * Time: 11:31
 * To change this template use File | Settings | File Templates.
 */
public class MostSimilarSElementOrTupleSubsequenceMapping extends MostSimilarSubsequenceMapping<SElementOrTuple<?>> {
    private List<SElementOrTuple<?>> X;
    private List<SElementOrTuple<?>> Y;
    private STools manager;

    public MostSimilarSElementOrTupleSubsequenceMapping () {}

    public MostSimilarSElementOrTupleSubsequenceMapping(STools m, List<SElementOrTuple<?>> l, List<SElementOrTuple<?>> r) {
        super();
        X = l;
        Y = r;
        manager = m;
    }

    @Override
    public void setX(List<SElementOrTuple<?>> xl) {
        X = xl;
    }

    @Override
    public void setY(List<SElementOrTuple<?>> yl) {
        Y = yl;
    }

    @Override
    public void initFrom(MostSimilarSubsequenceMapping<SElementOrTuple<?>> other) {
        if (other instanceof MostSimilarSElementOrTupleSubsequenceMapping) {
            manager = ((MostSimilarSElementOrTupleSubsequenceMapping) other).manager;
        }
    }

    @Override
    protected int lengthOfX() {
        return X.size();
    }

    @Override
    protected int lengthOfY() {
        return Y.size();
    }

    @Override
    protected SElementOrTuple<?> valueOfX(int index) {
        return X.get(index);
    }

    @Override
    protected SElementOrTuple<?> valueOfY(int index) {
        return Y.get(index);
    }

    @Override
    protected double similarity(SElementOrTuple<?> x1, SElementOrTuple<?> y1) {
        if (x1 instanceof SElementTuple && y1 instanceof SElementTuple) {
            return similarity((SElementTuple<?>) x1, (SElementTuple<?>) y1);
        } else if (x1 instanceof SElement && y1 instanceof SElement) {
            return similarity((SElement<?>) x1, (SElement<?>) y1);
        } else return 0.0;
    }

    protected double similarity(SElementTuple<?> x1, SElementTuple<?> y1) {
        if (x1.compatible(y1)) {
            double sim = 0.0;
            for (int i = 0; i < x1.size(); i++) {
                SElement<?> te = (SElement<?>) x1.get(i);
                SElement<?> oe = (SElement<?>) y1.get(i);
                SToolInterface s = manager.getSTool(te.getEquivSpec());
                sim = s.similarity(te, oe) + sim;
            }
            return sim;
        } else return 0.0;
    }

    protected double similarity(SElement<?> x1, SElement<?> y1) {
        //System.err.println("x1 = "+x1+"\nEuivspec = "+x1.getEquivspec()+"\nstool = "+manager.getSTool(x1.getEquivspec()));
        SToolInterface s = manager.getSTool(x1.getEquivSpec());
        //System.out.println(String.format("Similarity %s and %s = %s",x1,y1,s.similarity(x1,y1)));
        if (s==null) {
            System.err.println(String.format("Equivspec %s is undefined. Using default name-equivalence-only",
                    x1.getEquivSpec()));
            if (x1.namespace().equals(y1.namespace()) &&
                    x1.getType().equals(y1.getType()))
                return 1.0;
            else return 0.0;
        } else 
        return s.similarity(x1, y1);
    }

    @Override
    protected Boolean equals(SElementOrTuple<?> x1, SElementOrTuple<?> y1) {
        return (x1.equals(y1));
    }
}
