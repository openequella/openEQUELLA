package com.tle.web.api.institution

import com.tle.common.security.PrivilegeTree.Node
import com.tle.common.security.{PrivilegeTree, TargetList, TargetListEntry}
import com.tle.exceptions.PrivilegeRequiredException
import com.tle.legacy.LegacyGuice
import com.tle.web.api.interfaces.beans.security.{TargetListBean, TargetListEntryBean}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs._

import scala.collection.JavaConverters._

@Produces(value =Array("application/json"))
@Path("acl/")
@Api(value = "ACLs")
class AclResource {

	val aclManager = LegacyGuice.aclManager

	@GET
	@ApiOperation(value = "Get allowed privileges for tree node")
	@Path("/privileges") def getAllowedPrivileges(@QueryParam("node") node: Node) = {
		PrivilegeTree.getAllPrivilegesForNode(node).keySet().asScala.toVector.sorted
	}

	@GET
	@ApiOperation(value = "Get all institution level acls")
	@Path("/")
	def getEntries: TargetListBean = {
		checkPrivs("VIEW_SECURITY_TREE", "EDIT_SECURITY_TREE")
		val targetListBean = new TargetListBean
		val allAcls = aclManager.getTargetList(Node.INSTITUTION, null)
		val tBeanList = allAcls.getEntries.asScala.map { ae =>
			val tBean = new TargetListEntryBean
			tBean.setGranted(ae.isGranted)
			tBean.setOverride(ae.isOverride)
			tBean.setPrivilege(ae.getPrivilege)
			tBean.setWho(ae.getWho)
			tBean
		}
		targetListBean.setEntries(tBeanList.asJava)
		targetListBean
	}

	def checkPrivs(privs: String*): Unit = {
		if (aclManager.filterNonGrantedPrivileges(privs: _*).isEmpty) throw new PrivilegeRequiredException(privs: _*)
	}

	@PUT
	@ApiOperation(value = "Set all institution level acls")
	@Path("/")
	def setEntries(@ApiParam bean: TargetListBean): Response = {
		checkPrivs("EDIT_SECURITY_TREE")
		val tle = bean.getEntries.asScala.map { eb =>
			new TargetListEntry(eb.isGranted, eb.isOverride, eb.getPrivilege, eb.getWho)
		}
		aclManager.setTargetList(Node.INSTITUTION, null, new TargetList(tle.asJava))
		Response.status(Status.OK).build
	}
}
