package com.example

/**
 * Created by henry on 3/27/15.
 */
object ResourceProtocol {

  case class CreatingResource(path: List[String], content: AnyRef, owner: Option[String], groups: Option[Set[String]])

  case class ResourceState(content: AnyRef ,owner: String, groups: Set[String], children: Map[String, String] = Map[String, String]()) extends State
}
