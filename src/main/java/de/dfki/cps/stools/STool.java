/*
 * Copyright (c) Serge Autexier, Dominik Dietrich, DFKI GmbH 2010.
 */

package de.dfki.cps.stools;

import de.dfki.cps.stools.SElement;
import de.dfki.cps.stools.SElementConflict;
import de.dfki.cps.stools.SElementOrConflict;
import de.dfki.cps.stools.SToolInterface;
import de.dfki.cps.stools.editscript.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.dfki.cps.utils.Collectionxx;
import de.dfki.cps.utils.MostSimilarSubsequenceMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: autexier
 * Date: Nov 4, 2010
 * Time: 8:30:25 AM
 * To change this template use File | Settings | File Templates.
 */

public class STool implements SToolInterface {

    private static final Log log = LogFactory.getLog(STool.class);

    class SimilarityCache extends HashMap<SElement<?>, Map<SElement<?>, Double>> {
		private static final long serialVersionUID = 1L;
		
		int all;
        int success;

        public SimilarityCache() {
            super();
            all = 0;
            success = 0;
        }

        public Double cacheratio() {
            return (success / (all * 1.0));
        }

        public Integer allcaches() {
            return all;
        }

        public Double isCached(SElement<?> s, SElement<?> t) {
            all = all + 1;
            success = success + 1;
            if (this.containsKey(s) && this.get(s).containsKey(t)) return this.get(s).get(t);
            else if (this.containsKey(t) && this.get(t).containsKey(s)) return this.get(t).get(s);
            else {
                success = success - 1;
                return null;
            }
        }

        public void cache(SElement<?> s, SElement<?> t, Double d) {
            if (!this.containsKey(s)) put(s, new HashMap<SElement<?>, Double>());
            get(s).put(t, d);
        }

        public void reset() {
            super.clear();
        }
    }

    de.dfki.cps.stools.similarityspec.SimilaritySpec spec = null;
    STools manager;
    de.dfki.cps.stools.similarityspec.ElementSpecs limit;
    SimilarityCache similaritycache = null;

    public STool(de.dfki.cps.stools.similarityspec.SimilaritySpec s, STools st) {
        spec = s;
        manager = st;
        limit = null;
        similaritycache = new SimilarityCache();
    }

    public de.dfki.cps.stools.similarityspec.SimilaritySpec getSimilaritySpec() {
        return spec;
    }

    public double similarity(SElement<?> a, SElement<?> b) {
        assert (a.getEquivSpec().equals(b.getEquivSpec()));
        assert (a.getEquivSpec().equals(spec.getName()));
        //System.out.println("specname "+spec.getName());
        //System.out.println(String.format("A %s %s %s ",a.getNamespace(),a.getType(),a.getLabel()));
        //System.out.println(String.format("B %s %s %s ",b.getNamespace(),b.getType(),b.getLabel()));
        double result = 0.0;
        if (a.namespace().equals(b.namespace()) &&
                a.getType().equals(b.getType()) &&
                a.getLabel().equals(b.getLabel())) {
            List<de.dfki.cps.stools.similarityspec.ElementSimilaritySpec> specs =
                    spec.getElementSimilaritySpecs(a.namespace(), a.getType());
            //System.out.println("Specs = "+specs);
            //similaritycache.reset();
            for (de.dfki.cps.stools.similarityspec.ElementSimilaritySpec subspec : specs) {
                double temp = specSimilarity(subspec, a, b);
                if (temp > result) {
                    result = temp;
                    for (SElement<?> f : new SElement[]{a, b}) {
                        f.setSimilaritySpec(subspec);
                        for (SElement<?> c : (List<SElement<?>>) f.getChildren()) {
                            subspec.subelementSimSpec.setSubElementEquivSpec(c, spec.getName());
                        }
                    }
                }
            }

        }
        return result;
    }

    private double specSimilarity(de.dfki.cps.stools.similarityspec.ElementSimilaritySpec spec, SElement<?> a, SElement<?> b) {
        // To compare two SElements we proceed as follows:
        // 1) Annotations:
        //    if for some absent spec, one of the elements has that annotation, then we return 0.0.
        //    if some of the mandatory annotation is not there in some of them, we return 0.0
        //    otherwise we select all annotations indicated in the checkannotations and mandatory
        //    list and compute the maximum subset similarity where the similarity of two
        //    annotations is 1.0 if they have the same name and value.
        // 2) Children elements:
        //    TODO

        Double d = similaritycache.isCached(a, b);
        if (d == null) {
            double annotationsim = specAnnotationSimilarity(spec, a, b);
            //System.out.println(String.format("Annotation Sim %s and %s = %s", a, b, annotationsim));
            if (annotationsim > -1.0) {
                double elementsim = specElementSimilarity(spec, a.getChildren(), b.getChildren(), this.spec);
                //System.out.println("Checked children (" + this.spec.getName() + ") " + a.getChildren() + " vs. " + b.getChildren() + " = " + elementsim);

                d = elementsim + 1.0;
            } else {
                d = 0.0;
            }
            similaritycache.cache(a, b, d);
        }
        return d;
    }


    private double specAnnotationSimilarity(de.dfki.cps.stools.similarityspec.ElementSimilaritySpec spec, SElement<?> a, SElement<?> b) {
        // we return -1.0 if an absent annotation is present or a mandatory annotation is not.
        Double result = 0.0;
        de.dfki.cps.stools.similarityspec.AnnotationSimilaritySpec eass =
                spec.annotationSimSpec.expand(a, b, this.spec);
        // Absent
        for (de.dfki.cps.stools.similarityspec.AnnotationSpec as : eass.absent) {
            de.dfki.cps.stools.similarityspec.AnnotationNameSpec ans = (de.dfki.cps.stools.similarityspec.AnnotationNameSpec) as;
            if (a.hasAnnotation(ans.getNameSpace(), ans.getName()) ||
                    b.hasAnnotation(ans.getNameSpace(), ans.getName())) {
                result = -1.0;

            }
        }
        // Mandatory
        for (de.dfki.cps.stools.similarityspec.AnnotationSpec as : eass.mandatory) {
            //log.debug("Mandatory " + eass.mandatory);
            de.dfki.cps.stools.similarityspec.AnnotationNameSpec ans = (de.dfki.cps.stools.similarityspec.AnnotationNameSpec) as;
            if (!a.hasAnnotation(ans.getNameSpace(), ans.getName()) ||
                    !b.hasAnnotation(ans.getNameSpace(), ans.getName()) ||
                    !a.getAnnotation(ans.getNameSpace(), ans.getName()).value().equals(b.getAnnotation(ans.getNameSpace(), ans.getName()).value())) {
                result = -1.0;

            }
        }
        // Check
        //System.out.println("Checkking annotation sim "+eass.select(a)+" / "+eass.select(b));
        if (result != -1.0) {
            MostSimilarSAnnotationMapping msam =
                    new MostSimilarSAnnotationMapping(manager, eass.select(a), eass.select(b));
            //System.out.println("Sim = "+msam.getMSMSimilarity());
            return msam.getMSMSimilarity();
        } else return -1.0;
    }

    private double specElementSimilarity(de.dfki.cps.stools.similarityspec.ElementSimilaritySpec spec,
                                         List<SElement<?>> al,
                                         List<SElement<?>> bl,
                                         de.dfki.cps.stools.similarityspec.SimilaritySpec s) {
        // we return -1.0 if an absent annotation is present or a mandatory annotation is not.
        Double result = 0.0;
        if (al.size() == 0 && bl.size() == 0) result = 1.0;
        //System.err.println("SPECNAME " + this.spec.getName());
        de.dfki.cps.stools.similarityspec.SubElementSimilaritySpec ess =
                spec.subelementSimSpec.expand(this.spec.getName(), s, al, bl);
        // Absent
        for (de.dfki.cps.stools.similarityspec.ElementSpec as : ess.absent) {
            if (as instanceof de.dfki.cps.stools.similarityspec.ElementTupleSpec) {
                de.dfki.cps.stools.similarityspec.ElementTupleSpec n = (de.dfki.cps.stools.similarityspec.ElementTupleSpec) as;
                if (n.eval(al).size() > 0 || n.eval(bl).size() > 0) {
                    result = -1.0;
                    return -1.0;
                }
            } else if (as instanceof de.dfki.cps.stools.similarityspec.ElementNameSpec) {
                de.dfki.cps.stools.similarityspec.ElementNameSpec n = (de.dfki.cps.stools.similarityspec.ElementNameSpec) as;
                if (n.eval(al).size() > 0 || n.eval(bl).size() > 0) {
                    result = -1.0;
                    return -1.0;
                }
            }
        }
        // Mandatory
        for (de.dfki.cps.stools.similarityspec.ElementSpec as : ess.mandatory) {
            if (as instanceof de.dfki.cps.stools.similarityspec.ElementTupleSpec) {
                de.dfki.cps.stools.similarityspec.ElementTupleSpec n = (de.dfki.cps.stools.similarityspec.ElementTupleSpec) as;
                if (n.eval(al).size() == 0 || n.eval(bl).size() == 0 ||
                        (new MostSimilarSElementTupleSubsetMapping(manager, n.eval(al), n.eval(bl))).getMSMSimilarity() < 1.0) {
                    result = -1.0;
                    return -1.0;
                }
            } else if (as instanceof de.dfki.cps.stools.similarityspec.ElementNameSpec) {
                de.dfki.cps.stools.similarityspec.ElementNameSpec n = (de.dfki.cps.stools.similarityspec.ElementNameSpec) as;
                if (n.eval(al).size() == 0 || n.eval(bl).size() == 0 ||
                        (new MostSimilarSElementSubsetMapping(this, n.eval(al), n.eval(bl))).getMSMSimilarity() < 1.0) {
                    result = -1.0;
                    return -1.0;
                }
            }
        }

        // ordered

        Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> alprojection = ess.ordered.projection(al);
        //System.out.println(result + "Ordered alprojection " + alprojection);
        Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> blprojection = ess.ordered.projection(bl);
        //System.out.println("Ordered blprojection " + blprojection);
        for (de.dfki.cps.stools.similarityspec.ElementSpec es : alprojection.keySet()) {
            if (!(alprojection.get(es).isEmpty() && blprojection.get(es).isEmpty())) {
                MostSimilarSElementOrTupleSubsequenceMapping m =
                        new MostSimilarSElementOrTupleSubsequenceMapping(manager, alprojection.get(es), blprojection.get(es));
                result = m.getMSMSimilarity() + result;
            }
        }

        // Unordered

        alprojection = ess.unordered.projection(al);
        blprojection = ess.unordered.projection(bl);
        for (de.dfki.cps.stools.similarityspec.ElementSpec es : alprojection.keySet()) {
            if (!(alprojection.isEmpty() && blprojection.isEmpty())) {
                MostSimilarSElementOrTupleSubsetMapping m1 =
                        new MostSimilarSElementOrTupleSubsetMapping(manager, alprojection.get(es), blprojection.get(es));
                result = m1.getMSMSimilarity() + result;
            }
        }


        return result;
    }


    // ============================================================
    //
    // Difference Analysis
    //
    // =============================================================

    public SEditScript sdiff(SElement<?> a, SElement<?> b) {
        similaritycache.reset();
        double sim = similarity(a, b);

        if (sim > 0.0) {
            limit = null;
            if (!spec.getLimit().isEmpty()) limit = spec.getLimit();
            SEditScript result = diffElements(a, b, null);
            log.debug(String.format("Cache ratio (#%s): %s", similaritycache.allcaches(),
                    similaritycache.cacheratio()));
            return result;
        } else {
            SEditScript script = new SEditScript();
            script.insert(null, new ReplaceElement(null, a, b));
            return script;
        }
    }

    private SEditScript diffAnnotations(SElement<?> a, SElement<?> b) {
        List<SAnnotation<?>> al = a.getAnnotations();
        List<SAnnotation<?>> bl = b.getAnnotations();
        MostSimilarSAnnotationDiffMapping m =
                new MostSimilarSAnnotationDiffMapping(manager, al, bl);
        SEditScript res = new SEditScript();
        ArrayList<SAnnotationOrConflict<?>> added = new ArrayList<SAnnotationOrConflict<?>>();
        ArrayList<SAnnotation<?>> deleted = new ArrayList<SAnnotation<?>>();
        List<MostSimilarSubsequenceMapping.DiffEntry<SAnnotation<?>>> diff = m.getDiff();
        for (int i = 0; i < al.size(); i++) {
            MostSimilarSubsequenceMapping.DiffEntry<SAnnotation<?>> d = diff.get(i);
            switch (d.getType()) {
                case ADD:
                    added.add(d.getValue());
                    break;
                case UPDATE:
                    res.insert(a, new UpdateAnnotation(a, al.get(i), d.getValue()));
                    break;
                case REMOVE:
                    deleted.add(d.getValue());
                    break;
            }
        }
        if (!added.isEmpty()) res.insert(a, new AppendAnnotations(a, added));
        if (!deleted.isEmpty()) res.insert(a, new RemoveAnnotations(a, deleted));

        return res;
    }

    private SEditScript diffElements(SElement<?> a, SElement<?> b, SElement<?> parent) {
        //System.out.println("DIFF "+a+" "+b+" sim = "+similarity(a, b));
        if (similarity(a, b) == 0.0) {
            SEditScript res = new SEditScript();
            res.insert(parent, new ReplaceElement(parent, a, b));
            return res;
        } else {
            de.dfki.cps.stools.similarityspec.ElementSpecs savelimit = limit;

            // TODO: Add limits
            //Debug.println("Similarity %s: %s vs %s ",similarity(a,b),a,b);
            SEditScript res = diffAnnotations(a, b);

            List<SElement<?>> al = a.getChildren();
            for (SElement<?> s : al)
                if (s.getSimilaritySpec() == null) {
                    s.setSimilaritySpec(spec.getElementSimilaritySpecs(s.namespace(), s.getLabel()).get(0));
                }
            List<SElement<?>> bl = b.getChildren();
            for (SElement<?> s : bl)
                if (s.getSimilaritySpec() == null)
                    s.setSimilaritySpec(spec.getElementSimilaritySpecs(s.namespace(), s.getLabel()).get(0));
            //System.out.println(String.format("Children a: %s", al));
            //System.out.println(String.format("Children b: %s", bl));
            assert (a.getSimilaritySpec() != null);
            de.dfki.cps.stools.similarityspec.ElementSimilaritySpec s = a.getSimilaritySpec();
            //Debug.println("ElementSimilaritySpec s = " + s);

            if (!s.subelementSimSpec.limit.isEmpty()) {
                if (limit == null) {
                    de.dfki.cps.stools.similarityspec.ElementSpecs temp = new de.dfki.cps.stools.similarityspec.ElementSpecs();
                    temp.addAll(s.subelementSimSpec.limit);
                    limit = temp;
                } else {
                    de.dfki.cps.stools.similarityspec.ElementSpecs temp = new de.dfki.cps.stools.similarityspec.ElementSpecs();
                    temp.addAll(limit);
                    temp.addAll(s.subelementSimSpec.limit);
                    limit = temp;
                }
            }
            if (s == null) {
                Double sim = similarity(a, b);
                //System.out.println(String.format("Computed similarity %s vs. %s = %s", a, b, sim));
            }
            //System.out.println("Subelementspec " + s.subelementSimSpec);
            de.dfki.cps.stools.similarityspec.SubElementSimilaritySpec ess =
                    s.subelementSimSpec.expand(this.spec.getName(), this.spec, al, bl);

            //System.out.println("Ordered "+ess.ordered);
            //System.out.println("Unordered "+ess.unordered);

            SEditScript res1 = new SEditScript();

            // Ordered

            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> alprojection = ess.ordered.projection(al);
            //System.out.println("ordered alprojection: " + alprojection);
            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> blprojection = ess.ordered.projection(bl);
            //System.out.println("ordered blprojection: " + blprojection);
            for (de.dfki.cps.stools.similarityspec.ElementSpec es : alprojection.keySet()) {
                MostSimilarSElementOrTupleSubsequenceMapping m = new MostSimilarSElementOrTupleSubsequenceMapping(manager, alprojection.get(es), blprojection.get(es));
                //System.out.println("Alprojection "+alprojection.get(es));
                //System.out.println("Blprojection "+blprojection.get(es));
                //System.out.println("m.diff = " + m.getDiff());
                // Inserting diffs and recurse
                diffInsertDiffsAndRecurse(a, alprojection.get(es), blprojection.get(es), m.getDiff(), res1);

            }
            // Unordered

            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> a2lprojection = ess.unordered.projection(al);
            //System.out.println("unordered alprojection: " + a2lprojection);
            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> b2lprojection = ess.unordered.projection(bl);
            //System.out.println("unordered blprojection: " + b2lprojection);
            for (de.dfki.cps.stools.similarityspec.ElementSpec es : a2lprojection.keySet()) {
                MostSimilarSElementOrTupleSubsetMapping m1 = new MostSimilarSElementOrTupleSubsetMapping(manager, a2lprojection.get(es), b2lprojection.get(es));
                //System.out.println("A2lprojection "+a2lprojection.get(es));
                //System.out.println("B2lprojection "+b2lprojection.get(es));
                //System.out.println("m1.diff = " + m1.getDiff());
                // Inserting diffs and recurse
                diffInsertDiffsAndRecurse(a, a2lprojection.get(es), b2lprojection.get(es), m1.getDiff(), res1);
            }

            if (!res1.isEmpty() && limit != null && limit.match(a, false)) {
                res = new SEditScript();
                res.insert(parent, new ReplaceElement(parent, a, b));
            } else res.insertAll(res1);

            limit = savelimit;
            //System.out.println(String.format("Diffelements %s and %s = %s", a, b, res));
            return res;
        }
    }


    private void diffInsertDiffsAndRecurse(SElement<?> a,
                                           List<SElementOrTuple<?>> alprojection, List<SElementOrTuple<?>> blprojection,
                                           List<MostSimilarSubsequenceMapping.DiffEntry<SElementOrTuple<?>>> diff,
                                           SEditScript res) {
        ArrayList<SElementOrConflict<?>> added = new ArrayList<SElementOrConflict<?>>();
        ArrayList<SElement<?>> deleted = new ArrayList<SElement<?>>();
        //Debug.println(String.format("AL %s = ", alprojection.size()) + alprojection);
        //Debug.println(String.format("BL %s = ", blprojection.size()) + blprojection);
        //Debug.println("DIFF = " + diff);
        Integer alpos = 0;
        for (int i = 0; i < diff.size(); i++) {
            MostSimilarSubsequenceMapping.DiffEntry<SElementOrTuple<?>> d = diff.get(i);
            //Debug.println(String.format("Next diff entry %s (alpos=%s): %s %s",i,alpos,d.getType(),d.getValue()));
            switch (d.getType()) {
                case ADD:
                    if (d.getValue() instanceof SElementTuple<?>) {
                        for (SElement<?> e : ((SElementTuple<?>) d.getValue())) {
                            added.add(e);
                        }
                    } else added.add((SElement<?>) d.getValue());
                    break;
                case REMOVE:
                    if (d.getValue() instanceof SElementTuple<?>) {
                        for (SElement<?> e : ((SElementTuple<?>) d.getValue())) {
                            deleted.add(e);
                        }
                    } else deleted.add((SElement<?>) d.getValue());
                    alpos = alpos + 1;
                    break;
                case NONE:
                    break;
                case UPDATE:
                    if (!added.isEmpty()) {
                        SElement<?> refelement;
                        if (alprojection.get(alpos) instanceof SElement)
                            refelement = (SElement<?>) alprojection.get(alpos);
                        else refelement = ((SElementTuple<?>) alprojection.get(alpos)).get(0);
                        res.insert(a, new InsertBefore(refelement, added));
                        added = new ArrayList<SElementOrConflict<?>>();
                    }
                    //Debug.println("d.getType() = "+d.getType());
                    if (d.getValue() instanceof SElementTuple<?>) {
                        SElementTuple<?> atuple = (SElementTuple<?>) alprojection.get(alpos);
                        SElementTuple<?> btuple = (SElementTuple<?>) d.getValue();
                        for (int j = 0; j < btuple.size(); j++) {
                            res.insertAll(diffElements(atuple.get(j), btuple.get(j), a));
                        }
                    } else res.insertAll(diffElements(
                            (SElement<?>) alprojection.get(alpos),
                            (SElement<?>) d.getValue(), a));
                    alpos = alpos + 1;
                    break;
            }
        }
        if (!added.isEmpty()) res.insert(a, new AppendElements(a, added));
        if (!deleted.isEmpty()) res.insert(a, new RemoveElements(a, deleted));
        //Debug.println("Return");
    }

    /** The (improved) Diff3 Algorithm, which can also be used for merging simply. */

    public SEditScript merge(SElement<?> a, SElement<?> b) {
        similaritycache.reset();
        double sim = similarity(a, b);
        SElement<?> o = null;

        if (sim > 0.0) {
            limit = null;
            if (!spec.getLimit().isEmpty()) limit = spec.getLimit();
            o = a.copy();
            SEditScript result = diff3Elements(o, a, b, null);
            log.debug(String.format("Cache ratio (#%s): %s", similaritycache.allcaches(),
                    similaritycache.cacheratio()));
            return result;
        } else {
            SEditScript script = new SEditScript();
            script.insert(null,
                    new ReplaceElement(null, o,
                            new SElementConflict(
                                    SConflictType.List$.MODULE$,
                                    Collectionxx.newList(a),
                                    Collectionxx.newList(b))));
            return null;
        }
    }

    public SEditScript sdiff3(SElement<?> o, SElement<?> a, SElement<?> b) {
        similaritycache.reset();
        double sim = similarity(a, b);
        double simOA = similarity(o, a);
        double simeOB = similarity(o, b);

        if (sim > 0.0) {
            if (simOA == 0 || simeOB == 0) {
                return merge(a,b);
            } else {
                limit = null;
                if (!spec.getLimit().isEmpty()) limit = spec.getLimit();
                SEditScript result = diff3Elements(o, a, b, null);
                log.debug(String.format("Cache ratio (#%s): %s", similaritycache.allcaches(),
                        similaritycache.cacheratio()));
                return result;
            }
        } else {
            SEditScript script = new SEditScript();
            script.insert(null, new ReplaceElement(null, o,
                    new SElementConflict(
                            SConflictType.List$.MODULE$,
                            Collectionxx.newList(a),
                            Collectionxx.newList(b))));
            return null;
        }
    }

    private SAnnotationConflict toSAnnotationConflict(MostSimilarSubsequenceMapping.ConflictEntry<SAnnotation> c) {
        SConflictType type = null;
        if (c instanceof MostSimilarSubsequenceMapping.ListConflictEntry)
            type = SConflictType.List$.MODULE$;
        else if (c instanceof MostSimilarSubsequenceMapping.SetConflictEntry)
            type = SConflictType.Set$.MODULE$;
        else if (c instanceof MostSimilarSubsequenceMapping.UpdateDeleteConflictEntry)
            type = SConflictType.UpdateDelete$.MODULE$;
        return new SAnnotationConflict(type, c.getLeftvalues(), c.getRightvalues());
    }

    private SElementConflict<?> toSElementConflict(MostSimilarSubsequenceMapping.ConflictEntry<SElementOrTuple<?>> c) {
        SConflictType type = null;
        if (c instanceof MostSimilarSubsequenceMapping.ListConflictEntry)
            type = SConflictType.List$.MODULE$;
        else if (c instanceof MostSimilarSubsequenceMapping.SetConflictEntry)
            type = SConflictType.Set$.MODULE$;
        else if (c instanceof MostSimilarSubsequenceMapping.UpdateDeleteConflictEntry)
            type = SConflictType.UpdateDelete$.MODULE$;
        List<SElement<?>> leftelements = new ArrayList<SElement<?>>();
        for (SElementOrTuple<?> x : c.getLeftvalues()) {
            if (x instanceof SElementTuple) leftelements.addAll(((SElementTuple<?>) x));
            else leftelements.add(((SElement<?>) x));
        }
        List<SElement<?>> rightelements = new ArrayList<SElement<?>>();
        for (SElementOrTuple<?> x : c.getRightvalues()) {
            if (x instanceof SElementTuple) leftelements.addAll(((SElementTuple<?>) x));
            else leftelements.add(((SElement<?>) x));
        }
        return new SElementConflict(type, leftelements, rightelements);
    }

    private SEditScript diff3Annotations(SElement<?> o, SElement<?> a, SElement<?> b) {
        List<SAnnotation<?>> ol = (o == null) ? new ArrayList<SAnnotation<?>>(0) : o.getAnnotations();
        List<SAnnotation<?>> al = a.getAnnotations();
        List<SAnnotation<?>> bl = b.getAnnotations();
        MostSimilarSAnnotationDiffMapping m =
                new MostSimilarSAnnotationDiffMapping(manager, al, bl);
        SEditScript res = new SEditScript();
        ArrayList<SAnnotationOrConflict<?>> added = new ArrayList<SAnnotationOrConflict<?>>();
        ArrayList<SAnnotation<?>> deleted = new ArrayList<SAnnotation<?>>();
        List<MostSimilarSubsequenceMapping.DiffEntry<SAnnotation<?>>> diff3 =
                m.getDiff3((SAnnotation<?>[]) ol.toArray(new SAnnotation<?>[ol.size()]));
        for (int i = 0; i < diff3.size(); i++) {
            MostSimilarSubsequenceMapping.DiffEntry<SAnnotation<?>> d = diff3.get(i);
            switch (d.getType()) {
                case ADD:
                    if (d.getTwovalues()) {
                        // Conflict
                        added.add(new SAnnotationConflict(SConflictType.Set$.MODULE$, Collectionxx.newList(d.getValue()), Collectionxx.newList(d.getOthervalue())));
                    } else // No Conflict
                        added.add(d.getValue());
                    break;
                case UPDATE:
                    //System.out.println("ANNOT UPDATE " + d);
                    SAnnotation annot = null;
                    if (o != null) annot = o.getAnnotation(d.getValue().namespace(), d.getValue().name());
                    if (d.getTwovalues()) {
                        // Conflict
                        res.insert(o, new UpdateAnnotation(o, annot, new SAnnotationConflict(SConflictType.Set$.MODULE$, Collectionxx.newList(d.getValue()), Collectionxx.newList(d.getOthervalue()))));
                    } else // No Conflict
                        res.insert(o, new UpdateAnnotation(o, annot, d.getValue()));
                    break;
                case REMOVE:
                    deleted.add(d.getValue());
                    break;
                case CONFLICT:
                    MostSimilarSubsequenceMapping.ConflictEntry ce = (MostSimilarSubsequenceMapping.ConflictEntry) d;
                    added.add(toSAnnotationConflict(ce));
            }
        }
        if (!added.isEmpty()) res.insert(o, new AppendAnnotations(o, added));
        if (!deleted.isEmpty()) res.insert(o, new RemoveAnnotations(o, deleted));

        return res;
    }

    private SEditScript diff3Elements(SElement<?> o, SElement<?> a, SElement<?> b, SElement<?> parent) {
        //System.out.println(String.format("Diff3: %s : %s vs %s",o,a,b));
        if (similarity(a, b) == 0.0) {
            SEditScript res = new SEditScript();
            res.insert(parent, new ReplaceElement(parent, o,
                    new SElementConflict(
                            SConflictType.List$.MODULE$,
                            Collectionxx.newList(a),
                            Collectionxx.newList(b))));
            return res;
        } else {
            de.dfki.cps.stools.similarityspec.ElementSpecs savelimit = limit;

            // TODO: Add limits
            //System.out.println(String.format("Similarity %s: %s vs %s ",similarity(a,b),a,b));
            SEditScript res = diff3Annotations(o, a, b);
            List<SElement<?>> ol = (o == null) ? new ArrayList<SElement<?>>(0) : o.getChildren();
            List<SElement<?>> al = a.getChildren();
            List<SElement<?>> bl = b.getChildren();

            for (List<SElement<?>> xl : new List[]{ol, al, bl}) {
                for (SElement<?> s : xl)
                    if (s.getSimilaritySpec() == null) {
                        s.setSimilaritySpec(spec.getElementSimilaritySpecs(s.namespace(), s.getLabel()).get(0));
                    }
            }

            assert (a.getSimilaritySpec() != null);
            de.dfki.cps.stools.similarityspec.ElementSimilaritySpec s = a.getSimilaritySpec();
            log.debug("ElementSimilaritySpec s = " + s);

            if (!s.subelementSimSpec.limit.isEmpty()) {
                if (limit == null) {
                    de.dfki.cps.stools.similarityspec.ElementSpecs temp = new de.dfki.cps.stools.similarityspec.ElementSpecs();
                    temp.addAll(s.subelementSimSpec.limit);
                    limit = temp;
                } else {
                    de.dfki.cps.stools.similarityspec.ElementSpecs temp = new de.dfki.cps.stools.similarityspec.ElementSpecs();
                    temp.addAll(limit);
                    temp.addAll(s.subelementSimSpec.limit);
                    limit = temp;
                }
            }
            if (o != null && o.getSimilaritySpec() == null) {
                Double sim = similarity(o, a);
                //System.out.println(String.format("Computed similarity %s vs. %s = %s", a, b, sim));
            }
            //System.out.println("Subelementspec " + s.subelementSimSpec);
            de.dfki.cps.stools.similarityspec.SubElementSimilaritySpec ess =
                    s.subelementSimSpec.expand(this.spec.getName(), this.spec, ol, al, bl);

            //System.out.println("Ordered "+ess.ordered);
            //System.out.println("Unordered "+ess.unordered);

            SEditScript res1 = new SEditScript();

            // Ordered

            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> olprojection = ess.ordered.projection(ol);
            //System.out.println("ordered olprojection: " + olprojection);
            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> alprojection = ess.ordered.projection(al);
            //System.out.println("ordered alprojection: " + alprojection);
            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> blprojection = ess.ordered.projection(bl);
            //System.out.println("ordered blprojection: " + blprojection);
            for (de.dfki.cps.stools.similarityspec.ElementSpec es : alprojection.keySet()) {
                MostSimilarSElementOrTupleSubsequenceMapping m =
                        new MostSimilarSElementOrTupleSubsequenceMapping(
                                manager,
                                alprojection.get(es),
                                blprojection.get(es));
                //System.out.println("Alprojection "+alprojection.get(es));
                //System.out.println("Blprojection "+blprojection.get(es));
                //System.out.println("olprojection = "+olprojection.get(es));
                //System.out.println("m.diff3 = " + m.getDiff3((SElementOrTuple[]) olprojection.get(es).toArray(new SElementOrTuple[olprojection.get(es).size()])));
                // Inserting diffs and recurse
                diff3InsertDiffsAndRecurse(o,
                        olprojection.get(es), alprojection.get(es), blprojection.get(es),
                        m.getDiff3((SElementOrTuple[]) olprojection.get(es).toArray(new SElementOrTuple[olprojection.get(es).size()])),
                        res1);
            }
            // Unordered

            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> o2lprojection = ess.unordered.projection(ol);
            //System.out.println("unordered alprojection: " + a2lprojection);
            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> a2lprojection = ess.unordered.projection(al);
            //System.out.println("unordered alprojection: " + a2lprojection);
            Map<de.dfki.cps.stools.similarityspec.ElementSpec, List<SElementOrTuple<?>>> b2lprojection = ess.unordered.projection(bl);
            //System.out.println("unordered blprojection: " + b2lprojection);
            for (de.dfki.cps.stools.similarityspec.ElementSpec es : a2lprojection.keySet()) {
                MostSimilarSElementOrTupleSubsetMapping m1 =
                        new MostSimilarSElementOrTupleSubsetMapping(
                                manager, a2lprojection.get(es), b2lprojection.get(es));

                //System.out.println("B2lprojection "+b2lprojection.get(es));
                //System.out.println("m1.diff = " + m1.getDiff());
                // Inserting diffs and recurse
                diff3InsertDiffsAndRecurse(o,
                        o2lprojection.get(es), a2lprojection.get(es), b2lprojection.get(es),
                        m1.getDiff3((SElementOrTuple[]) o2lprojection.get(es).toArray(new SElementOrTuple[o2lprojection.get(es).size()])),
                        res1);
            }

            if (!res1.isEmpty() && limit != null && limit.match(a, false)) {
                res = new SEditScript();
                res.insert(parent,
                        new ReplaceElement(parent, o,
                        		// TODO: Check equality and avoid conflict!
                                        new SElementConflict(
                                        SConflictType.List$.MODULE$,
                                        Collectionxx.newList(a),
                                        Collectionxx.newList(b))))
                ;
            } else res.insertAll(res1);

            limit = savelimit;
            //System.out.println(String.format("Diff3elements %s %s and %s = %s", o, a, b, res));
            return res;
        }
    }

    private void diff3InsertDiffsAndRecurse(SElement<?> o,
                                            List<SElementOrTuple<?>> olprojection, List<SElementOrTuple<?>> alprojection, List<SElementOrTuple<?>> blprojection,
                                            List<MostSimilarSubsequenceMapping.DiffEntry<SElementOrTuple<?>>> diff,
                                            SEditScript res) {
        ArrayList<SElementOrConflict<?>> added = new ArrayList<SElementOrConflict<?>>();
        ArrayList<SElement<?>> deleted = new ArrayList<SElement<?>>();

        Integer olpos = 0;
        for (int i = 0; i < diff.size(); i++) {
            MostSimilarSubsequenceMapping.DiffEntry<SElementOrTuple<?>> d = diff.get(i);
            //log.debug(String.format("Next diff entry %s (alpos=%s): %s %s %s", i, olpos, d.getType(), d.getValue(), d.getOthervalue()));
            switch (d.getType()) {
                case ADD:
                    if (d.getTwovalues()) {
                        // we have and add of two object, that still need to be analyzed.

                        if (d.getValue() instanceof SElementTuple<?>) {
                            //SElementTuple otuple = (SElementTuple) olprojection.get(olpos);
                            SElementTuple<?> atuple = (SElementTuple<?>) d.getValue();
                            SElementTuple<?> btuple = (SElementTuple<?>) d.getOthervalue();
                            for (int j = 0; j < btuple.size(); j++) {
                                SElement<?> acopy = atuple.get(j).copy();
                                acopy.setEditScript(
                                    diff3Elements(acopy, atuple.get(j), btuple.get(j), o));
                                added.add(acopy);
                                }
                        } else {
                            SElement<?> acopy = ((SElement<?>) d.getValue()).copy();
                            acopy.setEditScript(diff3Elements(
                                acopy,
                                (SElement<?>) d.getValue(),
                                (SElement<?>) d.getOthervalue(),
                                o));
                            added.add(acopy);
                        }
                    } else {
                        if (d.getValue() instanceof SElementTuple<?>) {
                            added.addAll(((SElementTuple<?>) d.getValue()));
                        } else added.add((SElement<?>) d.getValue());
                    }
                    break;
                case REMOVE:
                    if (d.getValue() instanceof SElementTuple<?>) {
                        for (SElement<?> e : ((SElementTuple<?>) d.getValue())) {
                            deleted.add(e);
                        }
                    } else deleted.add((SElement<?>) d.getValue());
                    olpos = olpos + 1;
                    break;
                case NONE:
                    break;
                case CONFLICT:
                    if (!added.isEmpty()) {
                        SElement refelement;
                        if (olprojection.get(olpos) instanceof SElement<?>)
                            refelement = (SElement<?>) olprojection.get(olpos);
                        else refelement = ((SElementTuple<?>) olprojection.get(olpos)).get(0);
                        //System.out.println(String.format("Insert before %s : %s",refelement,added));
                        res.insert(o, new InsertBefore(refelement, added));
                        added = new ArrayList<SElementOrConflict<?>>();
                    }
                    MostSimilarSubsequenceMapping.ConflictEntry<SElementOrTuple<?>> ce =
                            (MostSimilarSubsequenceMapping.ConflictEntry<SElementOrTuple<?>>) d;

                    //deleted.add((SElement) olprojection.get(olpos));
                    //added.add(toSElementConflict(ce));

                    res.insert(o,
                            new ReplaceElement(o,(SElement<?>) olprojection.get(olpos),
                                    toSElementConflict(ce)));
                    olpos = olpos + 1;
                    break;
                case UPDATE:
                    if (!added.isEmpty()) {
                        SElement<?> refelement;
                        if (olprojection.get(olpos) instanceof SElement)
                            refelement = (SElement<?>) olprojection.get(olpos);
                        else refelement = ((SElementTuple<?>) olprojection.get(olpos)).get(0);
                        //System.out.println(String.format("Insert before %s : %s",refelement,added));
                        res.insert(o, new InsertBefore(refelement, added));
                        added = new ArrayList<SElementOrConflict<?>>();
                    }

                    if (d.getTwovalues()) {
                        // we have and add of two object, that still need to be analyzed.
                        if (d.getValue() instanceof SElementTuple) {
                            SElementTuple<?> otuple = (SElementTuple<?>) olprojection.get(olpos);
                            SElementTuple<?> atuple = (SElementTuple<?>) d.getValue();
                            SElementTuple<?> btuple = (SElementTuple<?>) d.getOthervalue();
                            for (int j = 0; j < btuple.size(); j++) {
                                res.insertAll(diff3Elements(otuple.get(j), atuple.get(j), btuple.get(j), o));
                            }
                        } else res.insertAll(diff3Elements(
                                (SElement<?>) olprojection.get(olpos),
                                (SElement<?>) d.getValue(),
                                (SElement<?>) d.getOthervalue(),
                                o));
                    } else {
                        //Debug.println("d.getType() = "+d.getType());
                        if (d.getValue() instanceof SElementTuple<?>) {
                            SElementTuple<?> otuple = (SElementTuple<?>) olprojection.get(olpos);
                            SElementTuple<?> tuple = (SElementTuple<?>) d.getValue();
                            for (int j = 0; j < tuple.size(); j++) {
                                res.insertAll(diffElements(otuple.get(j), tuple.get(j), o));
                            }
                        } else res.insertAll(diffElements(
                                (SElement<?>) olprojection.get(olpos),
                                (SElement<?>) d.getValue(), o));
                    }
                    olpos = olpos + 1;
                    break;
            }
        }
        if (!added.isEmpty()) res.insert(o, new AppendElements(o, added));
        if (!deleted.isEmpty()) res.insert(o, new RemoveElements(o, deleted));
        //Debug.println("Return");
    }

}
