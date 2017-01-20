/*
 * Copyright (c) Serge Autexier, Dominik Dietrich, DFKI GmbH 2011.
 */

package de.dfki.cps.stools;

import de.dfki.cps.stools.SElement;
import de.dfki.cps.utils.MostSimilarSubsequenceMapping;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: autexier
 * Date: 14.02.2011
 * Time: 11:31
 * To change this template use File | Settings | File Templates.
 */
public class MostSimilarSElementSubsequenceMapping extends MostSimilarSubsequenceMapping<SElement<?>> {
    private List<SElement<?>> X;
    private List<SElement<?>> Y;
    protected STool ST;

    public MostSimilarSElementSubsequenceMapping () {}

    public MostSimilarSElementSubsequenceMapping(STool st, List<SElement<?>> l, List<SElement<?>> r) {
        super();
        X = l;
        Y = r;
        ST = st;
    }

    @Override
    public void setX(List<SElement<?>> xl) {
        X = xl;
    }

    @Override
    public void setY(List<SElement<?>> yl) {
        Y = yl;
    }

    @Override
    public void initFrom(MostSimilarSubsequenceMapping<SElement<?>> other) {
        if (other instanceof MostSimilarSElementSubsequenceMapping) {
            ST = ((MostSimilarSElementSubsequenceMapping) other).ST;
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
    protected SElement<?> valueOfX(int index) {
        return X.get(index);
    }

    @Override
    protected SElement<?> valueOfY(int index) {
        return Y.get(index);
    }

    @Override
    protected double similarity(SElement<?> x1, SElement<?> y1) {
        return ST.similarity(x1,y1);
    }

    @Override
    protected Boolean equals(SElement<?> x1, SElement<?> y1) {
        return (x1.underlying().equals(y1.underlying()));
    }
}
