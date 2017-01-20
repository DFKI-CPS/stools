package de.dfki.cps.stools

import de.dfki.cps.stools.editscript.SEditScript
import de.dfki.cps.stools.similarityspec.SimilaritySpec

trait SToolInterface {
  def similarity(a: SElement[_], b: SElement[_]): Double
  def sdiff(a: SElement[_], b: SElement[_]): SEditScript
  def sdiff3(o: SElement[_], a: SElement[_], b: SElement[_]): SEditScript
  def merge(a: SElement[_], b: SElement[_]): SEditScript
  def getSimilaritySpec(): SimilaritySpec
}