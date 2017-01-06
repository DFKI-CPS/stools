package de.dfki.cps.stools

import de.dfki.cps.stools.editscript.SEditScript
import de.dfki.cps.stools.similarityspec.SimilaritySpec

trait SToolInterface {
  def similarity(a: ISElement[_], b: ISElement[_]): Double
  def sdiff(a: ISElement[_], b: ISElement[_]): SEditScript
  def sdiff3(o: ISElement[_], a: ISElement[_], b: ISElement[_]): SEditScript
  def merge(a: ISElement[_], b: ISElement[_]): SEditScript
  def getSimilaritySpec(): SimilaritySpec
}