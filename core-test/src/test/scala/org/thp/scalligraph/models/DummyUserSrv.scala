package org.thp.scalligraph.models

import scala.util.{Success, Try}

import play.api.mvc.RequestHeader

import org.thp.scalligraph.auth.{AuthContext, AuthContextImpl, Permission, UserSrv}

case class DummyUserSrv(
    userId: String = "admin",
    userName: String = "default admin user",
    organisation: String = "default",
    permissions: Set[Permission] = Set.empty,
    requestId: String = "testRequest"
) extends UserSrv { userSrv =>

  val authContext: AuthContext                                                                                            = AuthContextImpl(userSrv.userId, userSrv.userName, userSrv.organisation, userSrv.requestId, userSrv.permissions)
  override def getAuthContext(request: RequestHeader, userId: String, organisationName: Option[String]): Try[AuthContext] = Success(authContext)

  override def getSystemAuthContext: AuthContext = authContext
}
