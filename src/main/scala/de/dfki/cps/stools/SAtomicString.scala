package de.dfki.cps.stools

import java.util.ArrayList

import de.dfki.cps.stools.similarityspec.ElementSimilaritySpec

import scala.beans.BeanProperty

class SAtomicString(
    line: String, 
    @BeanProperty var equivSpec: String, 
    @BeanProperty val parent: ISElement[_]) extends SElement[String] {      
  override def copy() = {
    val c = new SAtomicString("",equivSpec,null)
    c.setSimilaritySpec(similaritySpec)
    c;
  }
  
  override def getObject() = line

  override def toString() = s"[AString '$line']"
  
  override def getChildren() = new ArrayList[ISElement[_]]

  override def getType() = "<TEXT>"

  override def getNamespace() = ""

  override def getLabel() = line

  override def getAnnotations = new ArrayList[SAnnotation[_]]

  override def hasAnnotation(namespace: String, name: String) = false

  override def getAnnotation(namespace: String, name: String) = null
 
  @BeanProperty
  var similaritySpec: ElementSimilaritySpec = null
}
