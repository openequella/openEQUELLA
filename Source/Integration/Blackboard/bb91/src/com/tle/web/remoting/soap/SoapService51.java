/**
 * SoapService51.java This file was auto-generated from WSDL by the Apache Axis
 * 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tle.web.remoting.soap;

public interface SoapService51 extends java.rmi.Remote
{
	public void deleteGroup(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String getContributableCollections() throws java.rmi.RemoteException, java.lang.Exception;

	public java.lang.String getCollection(java.lang.String in0) throws java.rmi.RemoteException, java.lang.Exception;

	public void editTopic(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;

	public java.lang.String getComment(java.lang.String in0, int in1, java.lang.String in2)
		throws java.rmi.RemoteException;

	public java.lang.String getSchema(java.lang.String in0) throws java.rmi.RemoteException, java.lang.Exception;

	public java.lang.String searchGroups(java.lang.String in0) throws java.rmi.RemoteException;

	public void deleteComment(java.lang.String in0, int in1, java.lang.String in2) throws java.rmi.RemoteException;

	public void addGroup(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;

	public java.lang.String searchItems(java.lang.String in0, java.lang.String[] in1, java.lang.String in2,
		boolean in3, int in4, boolean in5, int in6, int in7) throws java.rmi.RemoteException, java.lang.Exception;

	public java.lang.String cloneItem(java.lang.String in0, int in1, boolean in2) throws java.rmi.RemoteException;

	public void unlock(java.lang.String in0, int in1) throws java.rmi.RemoteException, java.lang.Exception;

	public void deleteItem(java.lang.String in0, int in1) throws java.rmi.RemoteException, java.lang.Exception;

	public java.lang.String facetCount(java.lang.String in0, java.lang.String[] in1, java.lang.String in2,
		java.lang.String[] in3) throws java.rmi.RemoteException, java.lang.Exception;

	public void deleteUser(java.lang.String in0) throws java.rmi.RemoteException;

	public boolean userExists(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String rejectTask(java.lang.String in0, int in1, java.lang.String in2, java.lang.String in3,
		java.lang.String in4, boolean in5) throws java.rmi.RemoteException, java.lang.Exception;

	public java.lang.String login(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
		java.lang.Exception;

	public java.lang.String getTopic(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String getGroupsByUser(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String[] getItemFilenames(java.lang.String in0, int in1, java.lang.String in2, boolean in3)
		throws java.rmi.RemoteException;

	public void addSharedOwner(java.lang.String in0, int in1, java.lang.String in2) throws java.rmi.RemoteException;

	public void logout() throws java.rmi.RemoteException;

	public void deleteTopic(java.lang.String in0) throws java.rmi.RemoteException;

	public boolean userNameExists(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String editItem(java.lang.String in0, int in1, boolean in2) throws java.rmi.RemoteException;

	public java.lang.String getComments(java.lang.String in0, int in1, int in2, int in3, int in4)
		throws java.rmi.RemoteException;

	public java.lang.String searchItemsFast(java.lang.String in0, java.lang.String[] in1, java.lang.String in2,
		boolean in3, int in4, boolean in5, int in6, int in7, java.lang.String[] in8) throws java.rmi.RemoteException,
		java.lang.Exception;

	public void archiveItem(java.lang.String in0, int in1) throws java.rmi.RemoteException, java.lang.Exception;

	public java.lang.String[] getTaskFilterNames() throws java.rmi.RemoteException;

	public void addUserToGroup(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;

	public void uploadFile(java.lang.String in0, java.lang.String in1, java.lang.String in2, boolean in3)
		throws java.rmi.RemoteException, java.lang.Exception;

	public void removeUserFromAllGroups(java.lang.String in0) throws java.rmi.RemoteException;

	public int queryCount(java.lang.String[] in0, java.lang.String in1) throws java.rmi.RemoteException,
		java.lang.Exception;

	public java.lang.String listTopics(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String saveItem(java.lang.String in0, boolean in1) throws java.rmi.RemoteException,
		java.lang.Exception;

	public java.lang.String getUser(java.lang.String in0) throws java.rmi.RemoteException, java.lang.Exception;

	public java.lang.String getGroupUuidForName(java.lang.String in0) throws java.rmi.RemoteException;

	public void deleteFile(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
		java.lang.Exception;

	public java.lang.String editUser(java.lang.String in0, java.lang.String in1, java.lang.String in2,
		java.lang.String in3, java.lang.String in4, java.lang.String in5) throws java.rmi.RemoteException;

	public boolean itemExists(java.lang.String in0, int in1) throws java.rmi.RemoteException;

	public java.lang.String createTopic(java.lang.String in0, java.lang.String in1, int in2)
		throws java.rmi.RemoteException;

	public java.lang.String addUser(java.lang.String in0, java.lang.String in1, java.lang.String in2,
		java.lang.String in3, java.lang.String in4, java.lang.String in5) throws java.rmi.RemoteException;

	public void moveTopic(java.lang.String in0, java.lang.String in1, int in2) throws java.rmi.RemoteException;

	public void removeSharedOwner(java.lang.String in0, int in1, java.lang.String in2) throws java.rmi.RemoteException;

	public java.lang.String loginWithToken(java.lang.String in0) throws java.rmi.RemoteException, java.lang.Exception;

	public void setParentGroupForGroup(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;

	public boolean isUserInGroup(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;

	public void editGroup(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;

	public java.lang.String newItem(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String getItem(java.lang.String in0, int in1, java.lang.String in2)
		throws java.rmi.RemoteException;

	public void addComment(java.lang.String in0, int in1, java.lang.String in2, int in3, boolean in4)
		throws java.rmi.RemoteException;

	public java.lang.String acceptTask(java.lang.String in0, int in1, java.lang.String in2, boolean in3)
		throws java.rmi.RemoteException, java.lang.Exception;

	public int[] queryCounts(java.lang.String[] in0, java.lang.String[] in1) throws java.rmi.RemoteException,
		java.lang.Exception;

	public java.lang.String newVersionItem(java.lang.String in0, int in1, boolean in2) throws java.rmi.RemoteException;

	public void keepAlive() throws java.rmi.RemoteException;

	public java.lang.String getTaskList(java.lang.String in0, int in1, int in2) throws java.rmi.RemoteException,
		java.lang.Exception;

	public java.lang.String searchUsersByGroup(java.lang.String in0, java.lang.String in1)
		throws java.rmi.RemoteException;

	public boolean groupExists(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String getSearchableCollections() throws java.rmi.RemoteException, java.lang.Exception;

	public void setOwner(java.lang.String in0, int in1, java.lang.String in2) throws java.rmi.RemoteException;

	public void removeUserFromGroup(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;

	public void unzipFile(java.lang.String in0, java.lang.String in1, java.lang.String in2)
		throws java.rmi.RemoteException, java.lang.Exception;

	public void removeAllUsersFromGroup(java.lang.String in0) throws java.rmi.RemoteException;

	public java.lang.String getTaskFilterCounts(boolean in0) throws java.rmi.RemoteException;

	public void cancelItemEdit(java.lang.String in0, int in1) throws java.rmi.RemoteException, java.lang.Exception;
}
