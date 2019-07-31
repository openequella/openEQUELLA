#!/bin/bash
set -o errexit
BACKUP=
HOST=lms-autotest
DBUSER=postgres
MOODLE=19

while [ $# -gt 0 ]
do
	case "$1" in
		(--host) HOST=$2; shift;;
		(--file) BACKUP=$2; shift;;
		(--moodle) MOODLE=$2; shift;;
	esac
	shift
done

#if [ -z $BACKUP ]
#then
#	BACKUP=moodle$MOODLE.backup
#fi

for mdl in 19 20 21 22 23 24 25 26 27
do	
	BACKUP=moodle$mdl.backup
	echo "Backing up database moodle$mdl on $HOST"
	pg_dump --host $HOST --port 5432 --username "$DBUSER" --format=c --compress=9 --file "./backup/$BACKUP" moodle$mdl
done

