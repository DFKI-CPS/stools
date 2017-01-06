/*
 * Copyright (c) Serge Autexier, Dominik Dietrich, DFKI GmbH 2011.
 */
package de.dfki.cps

import java.util.{HashMap, Iterator, Map}
import javax.xml.XMLConstants
import javax.xml.namespace.NamespaceContext

import scala.collection.JavaConverters._

/**
  * Created by IntelliJ IDEA.
  * User: autexier
  * Date: 19.01.2011
  * Time: 10:30
  * To change this template use File | Settings | File Templates.
  */
class PersonalNamespaceContext(val ns: Map[String, String]) extends NamespaceContext {
  val namespaces: Map[String, String] = {
    val res = new HashMap[String, String](ns.size)
    res.putAll(ns)
    res
  }

  override def toString: String = {
    var result: String = null
    if (namespaces.isEmpty) result = "PersonalNamespaceContext()"
    else {
      for (e <- namespaces.entrySet.asScala) {
        if (result == null) result = String.format("PersonalNamespaceContext(%s=%s", e.getKey, e.getValue)
        else result = result + String.format(",%s=%s", e.getKey, e.getValue)
      }
      result = result + ")"
    }
    return result
  }

  def getNamespaceURI(prefix: String): String = {
    var result: String = XMLConstants.NULL_NS_URI
    if (prefix == null) throw new NullPointerException("Null prefix")
    else

    /** else if (defaultprefix != null && namespaces.containsKey("") && prefix.equals(defaultprefix))
      * return namespaces.get("");   **/ if (namespaces.containsKey(prefix)) result = namespaces.get(prefix)
    else if (XMLConstants.XMLNS_ATTRIBUTE == prefix) result = XMLConstants.XMLNS_ATTRIBUTE_NS_URI
    else if (XMLConstants.XML_NS_PREFIX == prefix) result = XMLConstants.XML_NS_URI
    //Debug.println(prefix+" -> "+result);
    return result
  }

  def getPrefix(uri: String): String = {
    var result: String = null
    if (null == uri) throw new NullPointerException("Null uri")
    else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI == uri) result = XMLConstants.XMLNS_ATTRIBUTE
    else if (XMLConstants.XML_NS_URI == uri) result = XMLConstants.XML_NS_PREFIX
    else {
      namespaces.entrySet.asScala
        .find(e => e.getValue == uri)
        .foreach(e => result = e.getKey)
    }
    if (result != null) return result
    else if (uri == XMLConstants.NULL_NS_URI) return ""
    else {
      System.err.println("Unknown uri " + uri)
      //throw new UnsupportedOperationException();
      return uri
    }
  }

  // This method isn't necessary for XPath processing either.
  def getPrefixes(uri: String): Iterator[_] = {
    throw new UnsupportedOperationException
  }
}