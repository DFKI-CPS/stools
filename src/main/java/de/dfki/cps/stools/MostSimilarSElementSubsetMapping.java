/*
 * Copyright (c) Serge Autexier, Dominik Dietrich, DFKI GmbH 2011.
 */

package de.dfki.cps.stools;

import de.dfki.cps.utils.MostSimilarSubsetMapping;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: autexier
 * Date: 14.02.2011
 * Time: 11:31
 * To change this template use File | Settings | File Templates.
 */
public class MostSimilarSElementSubsetMapping extends MostSimilarSubsetMapping<SElement<?>> {
    private List<SElement<?>> X;
    private List<SElement<?>> Y;
    private STool ST;

    public MostSimilarSElementSubsetMapping() {}

    public MostSimilarSElementSubsetMapping(STool st,List<SElement<?>> l, List<SElement<?>> r) {
        super();
        X = l;
        Y = r;
        ST = st;
    }

    @Override
    protected int sizeOfX() {
        return X.size();
    }

    @Override
    protected int sizeOfY() {
        return Y.size();
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
    public void initFrom(MostSimilarSubsetMapping<SElement<?>> other) {
        if (other instanceof MostSimilarSElementSubsetMapping) {
            ST = ((MostSimilarSElementSubsetMapping) other).ST;
        }
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
