/*
 * Copyright (c) Serge Autexier, Dominik Dietrich, DFKI GmbH 2011.
 */
package de.dfki.cps.stools

import java.util.ArrayList

import de.dfki.cps.stools.similarityspec.ElementNameSpec
import scala.collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 * User: autexier
 * Date: 11.02.2011
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
trait SElementOrTuple[T]

class SElementTuple[T] extends ArrayList[SElement[_]] with SElementOrTuple[T] {
  implicit class CollectionImplicits[T](collection: java.util.Collection[T]) {
    def forEach(f: T => Unit) = ???
  }

  def contains(ens: ElementNameSpec): Boolean = {
    for (e <- this.asScala) {
      if ((e.namespace == ens.getNamespace) && (e.getType == ens.getName)) return true
    }
    false
  }

  def compatible(o: SElementTuple[_]): Boolean = {
    if (o.size == size) {
      {
        var i: Int = 0
        while (i < size) {
          {
            val te: SElement[_] = get(i)
            val oe: SElement[_] = o.get(i)
            if (!((te.namespace == oe.namespace) && (te.getType == oe.getType))) return false
          }
          i += 1; i - 1
        }
      }
      true
    }
    else false
  }

  def equals(o: SElementTuple[_]): Boolean = {
    if (o.size == size) {
      {
        var i: Int = 0
        while (i < size) {
          {
            val te: SElement[_] = get(i)
            val oe: SElement[_] = o.get(i)
            if (!(te == oe)) return false
          }
          i += 1; i - 1
        }
      }
      true
    }
    else false
  }
}