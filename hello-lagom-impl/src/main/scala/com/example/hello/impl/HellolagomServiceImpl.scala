package com.example.hello.impl

import akka.{Done, NotUsed}
import com.example.hello.api
import com.example.hello.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the HellolagomService.
  */
class HellolagomServiceImpl(external: ExternalService,persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext)
extends HellolagomService  {

 val userList = new ListBuffer[Posting]


 override def hello(id: String) = ServiceCall { _ =>
  // Look up the hello-lagom entity for the given ID.
  val ref = persistentEntityRegistry.refFor[HellolagomEntity](id)

  // Ask the entity the Hello command.
  ref.ask(Hello(id))
 }

 override def useGreeting(id: String) = ServiceCall { request =>
  // Look up the hello-lagom entity for the given ID.
  val ref = persistentEntityRegistry.refFor[HellolagomEntity](id)

  // Tell the entity to use the greeting message specified.
  ref.ask(UseGreetingMessage(request.message))
 }

 //  override def printNum(num: Int) = ServiceCall { request =>
 //    Future.successful(num + 5)
 //  }

 override def usingPostOperation: ServiceCall[Posting, ListBuffer[Posting]] = ServiceCall { request =>
  val add = Posting(request.name, request.id)
  userList += add
  Future.successful(userList)
 }

 override def readUser: ServiceCall[NotUsed, ListBuffer[Posting]] = ServiceCall { request =>
  Future.successful(userList)

 }

 override def deleteUser(id: Int) : ServiceCall[NotUsed, ListBuffer[Posting]] = ServiceCall { request =>
  val deleteRecord = userList.filter(x => x.id == id)
  userList --= deleteRecord
  Future.successful(userList)
 }

// override def updateUser(id: Int): ServiceCall[Posting, ListBuffer[Posting]] = ServiceCall { request =>
//  val updateRecord = userList.filter(x => x.id == id).result()
//  Future.successful(userList)
// }


 override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
  TopicProducer.singleStreamWithOffset {
   fromOffset =>
    persistentEntityRegistry.eventStream(HellolagomEvent.Tag, fromOffset)
     .map(ev => (convertEvent(ev), ev.offset))
  }

 private def convertEvent(helloEvent: EventStreamElement[HellolagomEvent]): api.GreetingMessageChanged = {
  helloEvent.event match {
   case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
  }
 }

 override def greetUser(msg: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
  Future.successful("Hi, " + msg)
 }

 override def testUser(): ServiceCall[NotUsed, UserData] = ServiceCall {request =>
  val result: Future[UserData] = external.getUser().invoke()
  result.map(response => UserData(response.userId,response.id,response.title,response.body))
 }
}
