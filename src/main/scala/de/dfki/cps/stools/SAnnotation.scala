package de.dfki.cps.stools

import scala.beans.BeanProperty

sealed trait SAnnotationOrConflict[T]

trait SAnnotation[T] extends SAnnotationOrConflict[T] {
  def underlying: T
  def name: String
  def value: String
  def namespace: String
  def parent: SElement[_]
}

case class SAnnotationConflict[T](
    @BeanProperty conflictType: SConflictType,
    @BeanProperty left: java.util.List[SAnnotation[_]],
    @BeanProperty right: java.util.List[SAnnotation[_]]) extends SAnnotationOrConflict[T] {
  override def toString() = {
    val form = 
      if (conflictType == SConflictType.List) "C(%s|%s)"
      else if (conflictType == SConflictType.UpdateDelete) "C<%s|%s>"
      else "C{%s|%s}"
    String.format(form,getLeft,getRight)
  }
}