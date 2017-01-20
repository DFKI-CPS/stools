package de.dfki.cps.stools

import java.util.ArrayList

import de.dfki.cps.stools.similarityspec.ElementSimilaritySpec

import scala.beans.BeanProperty

class SAtomicString(val underlying: String,
    equivSpecValue: String = null,
    val parent: SElement[_] = null) extends SElement[String] {
  this.equivSpec = equivSpecValue

  override def copy() = {
    val c = new SAtomicString("",equivSpec,null)
    c.setSimilaritySpec(similaritySpec)
    c;
  }

  def line = underlying

  override def toString() = s"[AString '$line']"
  
  val children = Nil
  val label = line
  val namespace = ""
  val annotations = Nil
  override def getType() = "<TEXT>"
}
