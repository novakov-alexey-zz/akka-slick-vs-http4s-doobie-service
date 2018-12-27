package org.alexeyn.json

trait GenericJsonWriter[T] {
  def toJsonString(e: T): String
}
