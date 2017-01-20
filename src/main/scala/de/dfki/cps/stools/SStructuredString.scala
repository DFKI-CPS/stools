package de.dfki.cps.stools

import java.util.ArrayList

import de.dfki.cps.stools.similarityspec.ElementSimilaritySpec

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class SStructuredString(
    val underlying: String,
    equivSpecValue: String,
    val parent: SElement[String]) extends SElement[String] {

  this.equivSpec = equivSpecValue

  val annotations = Nil
  val children = computedChildren
  val label = "<TEXT>"
  val namespace = ""

  def line = underlying

  def copy() = {
    val c = new SStructuredString("",equivSpec,null)        
    c.setSimilaritySpec(similaritySpec)
    c
  }

  override def toString() = s"[SString '$line']"
  
  lazy val computedChildren: Seq[SElement[_]] = {
    val cs: Array[SElement[_]] = equivSpec match {
      case "wordlist" | "wordset" =>
        line.split(" ").flatMap(Array(_," ")).init.map(new SAtomicString(_,"word",this))
      case "charsetlist" | "charsetset" =>
        line.split(" ").flatMap(Array(_," ")).init.map(new SStructuredString(_, "charset", this))
      case "charlistlist" | "charlistset" =>
        line.split(" ").flatMap(Array(_," ")).init.map(new SStructuredString(_, "charlist", this))
      case "charset" | "charlist" =>
        line.toCharArray().map(c => new SAtomicString(c.toString, "char", this))
      case _ =>              
        System.err.println(s"Nothing for $this and $equivSpec");
        Array()
    }
    cs.foreach(_.setEquivSpec(equivSpec))
    cs
  }
}
