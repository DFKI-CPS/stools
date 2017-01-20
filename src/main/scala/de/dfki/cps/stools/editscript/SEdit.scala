package de.dfki.cps.stools.editscript

import de.dfki.cps.stools._

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

trait SEdit

/*
 * @BeanProperty annotations are for Java Interop
 */

case class AppendAnnotations(
    @BeanProperty el: SElement[_],
    @BeanProperty annotations: java.util.List[SAnnotationOrConflict[_]]) extends SEdit {  
  override def toString = s"appendAnnotations($getAnnotations)"
  def +(other: AppendAnnotations) = {
    assert(el == other.el)
    AppendAnnotations(el, (annotations.asScala ++ other.annotations.asScala).asJava)
  }
}

case class AppendElements(
    @BeanProperty el: SElement[_],
    @BeanProperty elements: java.util.List[SElementOrConflict[_]]) extends SEdit {  
  override def toString = elements.asScala.map {
    case e: SElement[_] if !e.getEditScript.isEmpty => e.toString() + e.getEditScript
    case e => e.toString()
  }.mkString ("append(",",",")")  
}

case class InsertAfter(
    @BeanProperty ref: SElement[_],
    @BeanProperty elements: java.util.List[SElementOrConflict[_]]) extends SEdit {
  override def toString = s"insert-after($ref,$elements)"
}

case class InsertBefore(
    @BeanProperty ref: SElement[_],
    @BeanProperty elements: java.util.List[SElementOrConflict[_]]) extends SEdit {
  override def toString = s"insert-before($ref,$elements)"
}
    
case class RemoveAnnotations(
    @BeanProperty ref: SElement[_],
    @BeanProperty a: java.util.List[SAnnotation[_]]) extends SEdit {
  override def toString = s"removeAnnotations($a)"
}

case class RemoveElements(
    @BeanProperty ref: SElement[_],
    @BeanProperty el: java.util.List[SElement[_]]) extends SEdit {
  override def toString = s"remove($el)"
}

case class ReplaceElement(
    @BeanProperty ref: SElement[_],
    @BeanProperty oldElement: SElement[_],
    @BeanProperty newElement: SElementOrConflict[_]) extends SEdit {
  override def toString = s"replace($oldElement --> $newElement)"
}

case class UpdateAnnotation(
    @BeanProperty ref: SElement[_],
    @BeanProperty oldAnnotation: SAnnotation[_],
    @BeanProperty newAnnotation: SAnnotationOrConflict[_]) extends SEdit {
  override def toString = {
    val name = oldAnnotation.name
    val o = oldAnnotation.value
    val n = newAnnotation match {
      case a: SAnnotation[_] => a.value
      case c: SAnnotationConflict[_] => c.toString()
    }
    s"$name[$o|-->$n]"
  }
}
