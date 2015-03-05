package com.example

import net.hamnaberg.json.collection._
import org.json4s._
import spray.http._
import spray.httpx.unmarshalling._
import spray.http.MediaTypes._

object CollectionJsonProtocol
{
  val `application/vnd.collection+json` =
    MediaTypes.register(MediaType.custom("application/vnd.collection+json"))

}

trait CollectionJsonSupport {

  import CollectionJsonProtocol._

  //implicit val parser: JsonCollectionParser = NativeJsonCollectionParser

  implicit def templateUnmarshaller =
    Unmarshaller[Template](`application/vnd.collection+json`) {
      case HttpEntity.NonEmpty(contentType, data) =>
        NativeJsonCollectionParser.parseTemplate(data.asString).right.get
    }

  implicit def templateObjectUnmarshaller[T: Manifest] =
    Unmarshaller.delegate[Template, T](`application/vnd.collection+json`) { template =>
      implicit val formats = org.json4s.DefaultFormats
      new data.JavaReflectionData[T].unapply(template.data).get 
    }
}
