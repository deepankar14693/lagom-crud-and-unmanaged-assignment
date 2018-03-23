package com.example.hello.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

import scala.collection.mutable.ListBuffer

object HellolagomService {
 val TOPIC_NAME = "greetings"
}

/**
  * The hello-lagom service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the HellolagomService.
  */


trait HellolagomService extends Service {

 override final def descriptor = {
  import Service._
  // @formatter:off
  named("hello-lagom")
   .withCalls(
    pathCall("/api/hello/:id", hello _),
    //    pathCall("/api/number/:id", printNum _),
    restCall(Method.POST,"/api/add/",usingPostOperation _),
    pathCall("/api/hello/:id", useGreeting _),
    pathCall("/api/read/",readUser _),
    restCall(Method.GET,"/test/1", testUser _),
    pathCall("/api/delete/:id",deleteUser _),
    //restCall(Method.PUT,"/api/update/:id",updateUser _)
   )
   .withAutoAcl(true)
  // @formatter:on
 }

 def greetUser(msg: String): ServiceCall[NotUsed, String]

 def testUser(): ServiceCall[NotUsed, UserData]

 // adding new user and showing list as response
 def usingPostOperation() : ServiceCall[Posting, ListBuffer[Posting]]
 //reading all data from the list
 def readUser: ServiceCall[NotUsed,ListBuffer[Posting]]
 //deleting user based on particular id
 def deleteUser(id: Int): ServiceCall[NotUsed,ListBuffer[Posting]]
 //def updateUser(id: Int): ServiceCall[String,ListBuffer[Posting]]
 /*
   * Example: curl http://localhost:9000/api/hello/Alice
   */
 def hello(id: String): ServiceCall[NotUsed, String]

 //def printNum(id: Int): ServiceCall[NotUsed,Int]

 /**
   * Example: curl -H "Content-Type: application/json" -X  POST -d '{"message":
   * "Hi"}' http://localhost:9000/api/hello/Alice
   */
 def useGreeting(id: String): ServiceCall[GreetingMessage, Done]

 /**
   * This gets published to Kafka.
   */
 def greetingsTopic(): Topic[GreetingMessageChanged]
}

/**
  * The greeting message class.
  */
case class GreetingMessage(message: String)

object GreetingMessage {
 /**
   * Format for converting greeting messages to and from JSON.
   *
   * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
   */
 implicit val format: Format[GreetingMessage] = Json.format[GreetingMessage]
}


/**
  * The greeting message class used by the topic stream.
  * Different than [[GreetingMessage]], this message includes the name (id).
  */
case class GreetingMessageChanged(name: String, message: String)

object GreetingMessageChanged {
 /**
   * Format for converting greeting messages to and from JSON.
   *
   * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
   */
 implicit val format: Format[GreetingMessageChanged] = Json.format[GreetingMessageChanged]
}

/**this case class is used for CRUD application
  *
  * @param name user name
  * @param id user id
  */
case class Posting(name: String, id: Int)

object Posting {

 implicit val format: Format[Posting] = Json.format[Posting]

}

/**this case class is used for unmanaged services
  *
  * @param userId
  * @param id
  * @param title
  * @param body
  */
 case class UserData(userId: Int,id: Int,title: String,body: String)
 object UserData {

  implicit val format: Format[UserData] = Json.format[UserData]
 }

