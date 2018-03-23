package com.example.hello.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service.{named, pathCall}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

trait ExternalService extends Service {

 def getUser(): ServiceCall[NotUsed, UserData]

 override final def descriptor = {
  import Service._
  // @formatter:off
  named("external-service")
   .withCalls(
    pathCall("/posts/1", getUser _)
   ).withAutoAcl(true)
  // @formatter:on
 }
}
