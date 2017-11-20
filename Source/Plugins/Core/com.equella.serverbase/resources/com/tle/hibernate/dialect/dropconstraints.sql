DECLARE @NAME sysname
DECLARE @UNIQUE bit
DECLARE @SQL nvarchar(1000)

DECLARE indexeslist CURSOR FOR
SELECT i.name,
  i.is_unique_constraint
FROM sys.index_columns ic
INNER JOIN sys.indexes i ON ic.object_id = i.object_id
 AND ic.index_id = i.index_id
INNER JOIN sys.columns c ON ic.column_id = c.column_id
 AND i.object_id = c.object_id
WHERE i.object_id = object_id('$table')
 AND c.name = '$column'

OPEN indexeslist 
FETCH NEXT FROM indexeslist INTO @NAME, @UNIQUE

WHILE(@@FETCH_STATUS = 0)
BEGIN

  IF @UNIQUE = 1
    BEGIN
      SELECT @SQL = 'alter table $table drop constraint "' + @NAME + '"'
    END
  ELSE
    BEGIN
      SELECT @SQL = 'drop index $table.' + @NAME
    END 
    
  EXECUTE sp_executesql @SQL 
  FETCH NEXT FROM indexeslist INTO @NAME, @UNIQUE
END 
CLOSE indexeslist 
DEALLOCATE indexeslist

DECLARE fklist CURSOR FOR
SELECT distinct(
  object_name(constraint_object_id)) AS
cons
FROM sys.foreign_key_columns fk
INNER JOIN sys.columns c ON fk.parent_column_id = c.column_id
 AND fk.parent_object_id = c.object_id
WHERE c.name = '$column'
 AND c.object_id = object_id('$table')

OPEN fklist
FETCH NEXT FROM fklist INTO @NAME

WHILE(@@FETCH_STATUS = 0)
BEGIN
  SELECT @SQL = 'alter table $table drop constraint "' + @NAME + '"'
    
  EXECUTE sp_executesql @SQL 
  FETCH NEXT FROM fklist INTO @NAME
END 
CLOSE fklist 
DEALLOCATE fklist

