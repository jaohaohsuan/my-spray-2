package com.example

/**
 * Created by henry on 3/27/15.
 */
object ResourceProtocol {

  case class CreatingResource(path: List[String], owner: Option[String], groups: Option[Set[String]])

  case class ResourceState(owner: String, groups: Set[String], children: Set[String] = Set[String]()) extends State
}
