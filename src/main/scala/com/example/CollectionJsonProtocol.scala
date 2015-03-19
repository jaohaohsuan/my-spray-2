package com.example

import net.hamnaberg.json.collection._
import org.json4s.Formats
import org.json4s.native.JsonMethods._
import spray.http._
import spray.httpx.Json4sSupport
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._

object CollectionJsonProtocol {
  val `application/vnd.collection+json` =
    MediaTypes.register(MediaType.custom("application/vnd.collection+json"))
}

trait CollectionJsonSupport extends Json4sSupport {

  import com.example.CollectionJsonProtocol._

  implicit def json4sFormats: Formats = org.json4s.DefaultFormats

  implicit val templateUnmarshaller: Unmarshaller[Template] =
    Unmarshaller[Template](`application/vnd.collection+json`) {
      case HttpEntity.NonEmpty(contentType, data) =>

        val string = data.asString
        NativeJsonCollectionParser.parseTemplate(string) match {
          case Right(o: Template) => o
          case Left(e) =>
            throw e
        }
    }

  implicit def templateObjectUnmarshaller[T <: AnyRef: Manifest]: Unmarshaller[T] =
    Unmarshaller.delegate[Template, T](`application/vnd.collection+json`) { template =>
      new data.JavaReflectionData[T].unapply(template.data) match {
        case Some(o) => o
        case None =>
          throw new Exception(s"Unable to convert Template to '$manifest.getClass.getName' class.")
      }
    }

  //Do not change order of collectionJsonUnmarshaller & collectionJsonStringUnmarshaller

  implicit val collectionJsonUnmarshaller =
    Unmarshaller[JsonCollection](`application/vnd.collection+json`) {
      case HttpEntity.NonEmpty(contentType, data) =>
        NativeJsonCollectionParser.parseCollection(data.asString) match {
          case Right(o: JsonCollection) => o
          case Left(e) =>
            JsonCollection(java.net.URI.create("http://com.example/unmarshalling"), Error("Unmarshalling CollectionJson Error", None, Some(e.getMessage)))
        }
    }

  implicit val collectionJsonStringUnmarshaller: Unmarshaller[String] =
    Unmarshaller.delegate[JsonCollection, String](`application/vnd.collection+json`) { collection =>
      compact(render(collection.toJson))
    }

  implicit val stringMarshaller =
    Marshaller.of[String](`application/vnd.collection+json`) { (value, contentType, ctx) =>
      ctx.marshalTo(HttpEntity(contentType, value))
    }

  implicit val collectionJsonMarshaller: Marshaller[JsonCollection] =
    Marshaller.of[JsonCollection](`application/vnd.collection+json`) { (value, contentType, ctx) =>
      ctx.marshalTo(HttpEntity(contentType, compact(render(value.toJson))))
    }

}
