package de.dfki.cps.stools

sealed trait SConflictType

object SConflictType {
  case object List extends SConflictType
  case object Set extends SConflictType
  case object UpdateDelete extends SConflictType
}