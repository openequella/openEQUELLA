if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[FK_Membership_Group]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [dbo].[Membership] DROP CONSTRAINT FK_Membership_Group
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[FK_Membership_User]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [dbo].[Membership] DROP CONSTRAINT FK_Membership_User
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Group]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[Group]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Membership]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[Membership]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[User]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[User]
GO

CREATE TABLE [dbo].[Group] (
	[ID] [varchar] (255) NOT NULL ,
	[Name] [nvarchar] (255) NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[Membership] (
	[UserID] [varchar] (255) NOT NULL ,
	[GroupID] [varchar] (255) NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[User] (
	[ID] [varchar] (255) NOT NULL ,
	[Username] [nvarchar] (255) NOT NULL ,
	[FirstName] [nvarchar] (255) NOT NULL ,
	[LastName] [nvarchar] (255) NOT NULL ,
	[EmailAddress] [varchar] (255) NOT NULL ,
	[Role] [int] NOT NULL ,
	[Password] [nvarchar] (255) NOT NULL ,
	[Suspended] [int] NOT NULL 
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Group] ADD 
	CONSTRAINT [PK_Group] PRIMARY KEY  CLUSTERED 
	(
		[ID]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[Membership] ADD 
	CONSTRAINT [PK_Membership] PRIMARY KEY  CLUSTERED 
	(
		[UserID],
		[GroupID]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[User] ADD 
	CONSTRAINT [PK_User] PRIMARY KEY  CLUSTERED 
	(
		[ID]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[Membership] ADD 
	CONSTRAINT [FK_Membership_Group] FOREIGN KEY 
	(
		[GroupID]
	) REFERENCES [dbo].[Group] (
		[ID]
	),
	CONSTRAINT [FK_Membership_User] FOREIGN KEY 
	(
		[UserID]
	) REFERENCES [dbo].[User] (
		[ID]
	)
GO

