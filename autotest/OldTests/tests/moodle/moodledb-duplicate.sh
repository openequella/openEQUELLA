#!/bin/bash
set -o errexit
BACKUP=moodle19.backup
HOST=localhost
DBUSER=postgres

while [ $# -gt 0 ]
do
	case "$1" in
	(--host) HOST=$2; shift;;
	(--file) BACKUP=$2; shift;;
	(--user) DBUSER=$2; shift;;
	esac
	shift
done

if [ ! -f $BACKUP ] 
then
	echo "Error: File '$BACKUP' does not exist"; exit 1;
fi


for moodle in 19 20 21 22 23
do	
	echo "Droping database moodle$moodle on $HOST"
	psql --host $HOST --port 5432 --username "$DBUSER" --dbname "postgres" --command "drop database moodle$moodle;"
	echo "Creating database moodle$moodle on $HOST"
	psql --host $HOST --port 5432 --username "$DBUSER" --dbname "postgres" --command "create database moodle$moodle with encoding 'UTF8';"

	echo "Restoring database moodle$moodle on $HOST"
	pg_restore --host $HOST --port 5432 --username "$DBUSER" --dbname "moodle$moodle" --jobs 8 "$BACKUP"
	echo "Modifying settings..."
	psql --host $HOST --port 5432 --username "$DBUSER" --dbname "moodle$moodle" --command "update mdl_course set fullname = 'moodle$moodle', shortname = 'moodle$moodle' where id = 1;"
	psql --host $HOST --port 5432 --username "$DBUSER" --dbname "moodle$moodle" --command "update mdl_config set value = 'moodle$moodle' where name = 'sessioncookie';"
	psql --host $HOST --port 5432 --username "$DBUSER" --dbname "moodle$moodle" --command "update mdl_config set value = '/moodle$moodle/' where name = 'sessioncookiepath';"
	echo "Done"
done
