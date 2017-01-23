package de.dfki.cps.stools.similarityspec

abstract class AnnotationSpec {
  def getAllAnnotationNames: java.util.List[String]
  def isValid: java.lang.Boolean
}