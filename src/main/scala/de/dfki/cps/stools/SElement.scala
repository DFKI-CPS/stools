package de.dfki.cps.stools

import de.dfki.cps.stools.editscript.SEditScript
import de.dfki.cps.stools.similarityspec.ElementSimilaritySpec

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

sealed trait SElementOrConflict[T]

trait SElement[T] extends SElementOrTuple[T] with SElementOrConflict[T] {
  def underlying: T
  def children: Seq[SElement[_]]
  def label: String
  def namespace: String
  def annotations: Seq[SAnnotation[_]]
  def parent: SElement[_]
  def copy(): SElement[_]

  def getChildren(): java.util.List[SElement[_]] = children.asJava
  def getType(): String = label
  def getLabel(): String = label
  def getAnnotations(): java.util.List[SAnnotation[_]] = annotations.asJava
  def hasAnnotation(namespace: String, name: String): Boolean =
    annotations.exists(a => a.namespace == namespace && a.name == name)
  def getAnnotation(namespace: String, name: String): SAnnotation[_] =
    annotations.find(a => a.namespace == namespace && a.name == name).get

  @BeanProperty var equivSpec: String = null
  @BeanProperty var similaritySpec: ElementSimilaritySpec = null
  def getParent(): SElement[_] = parent

  protected var patch = new SEditScript

  def setEditScript(r: SEditScript) = {
    if (!patch.isEmpty) patch = new SEditScript()
    patch.insertAll(r)
  }

  def getEditScript() = patch
}

case class SElementConflict[T](
    @BeanProperty `type`: SConflictTypes,
    @BeanProperty left: java.util.List[SElement[_]],
    @BeanProperty right: java.util.List[SElement[_]]) extends SElementOrConflict[T] {
  override def toString = {
    val form =
      if (getType == SConflictTypes.list) "C(%s|%s)"
      else if (getType == SConflictTypes.updatedelete) "C<%s|%s>" 
      else "C{%s|%s}"
    String.format(form,getLeft,getRight);
  }
}

/*class SElementTuple[T] extends java.util.ArrayList[ISElement[_]] with SElementOrTuple[T] {
  def contains(ens: ElementNameSpec) = 
    this.exists { e => e.getNamespace() == ens.getNamespace && e.getType == ens.getName }
  
  def compatible(o: SElementTuple[_]) =
    (o.size == this.size) && 
    (this.zip(o).forall { case (a,b) => a.getNamespace == b.getNamespace && a.getType == b.getType })

  def equals(o: SElementTuple[_]) =
    (o.size == this.size) &&
    (this.zip(o).forall { case (a,b) => a == b })
}*/