package de.dfki.cps.stools.similarityspec

abstract class ElementSpec {
  def getAllTagNames: java.util.List[String]
  def isValid: java.lang.Boolean
}

abstract class ElementNameOrTupleSpec extends ElementSpec