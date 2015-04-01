package com.example

import net.hamnaberg.json.collection._
import net.hamnaberg.json.collection.data._
/**
 * Created by henry on 3/15/15.
 */
object implicitTemplateConversions {

  import scala.language.implicitConversions

  implicit def asTemplate[T <: AnyRef: Manifest](value: T)(implicit formats: org.json4s.Formats, dataApply: DataApply[T]): Option[Template] =
    Some(Template(value)(dataApply))

//  implicit def dataApply[T <: AnyRef: Manifest](implicit formats: org.json4s.Formats): DataApply[T] = {
//    new JavaReflectionData[T]()(formats, manifest[T])
//  }
}
