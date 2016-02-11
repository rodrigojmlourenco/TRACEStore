import sys
import mysql.connector as mariadb
import xml.etree.ElementTree as ET

config_path = "./config.xml"

config = ET.parse(config_path).getroot()

repository 	= None

#Database connection fields
_user 		= None
_password 	= None
_database	= None

for child in config:
	if child.tag == 'repository':
		repository = child;

for child in repository:
	if child.tag == 'user':
		_user = child.attrib['val']
	elif child.tag == 'password':
		_password = child.attrib['val']
	elif child.tag == 'database':
		_database = child.attrib['val']
	else:
		print "Error, unknown tag "+child.tag
		exit

mariadb_connection = mariadb.connect(user=_user, password=_password)
cursor = mariadb_connection.cursor()
database = 'TRACE'


print "The execution of this script will lead removal of all information currently stored in the database.\nAre your sure you want to proceed? (y/n)"

input = raw_input()

if input == 'n' or input == 'N':
	print "Exiting..."
	sys.exit()
	

cleanup = "DROP DATABASE IF EXISTS "+database
cursor.execute(cleanup)
mariadb_connection.commit()

print 'Setting up all TRACE\'s databases...'


cmd1 = "CREATE DATABASE IF NOT EXISTS "+database +" CHARACTER SET  = 'utf8' COLLATE = 'utf8_general_ci';"
cmd2 = 'USE '+database;
cursor.execute(cmd1)
cursor.execute(cmd2)

# Phase 1 - User Tables
print "Starting Phase 1 - Users' tables"

users	 = "CREATE TABLE IF NOT EXISTS users ("
users	+= "Id int(11) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY, "
users	+= "Username VARCHAR(20) NOT NULL UNIQUE, "
users	+= "Email varchar(128) NOT NULL UNIQUE, "
users	+= "Password varchar(128) NOT NULL, "
users	+= "Salt varchar(128) NOT NULL, "
users	+= "CreatedAt timestamp not null DEFAULT CURRENT_TIMESTAMP, "
users	+= "LastVisit timestamp DEFAULT 0)"

user_details	 = "CREATE TABLE IF NOT EXISTS user_details ("
user_details	+= "Id int(11) unsigned NOT NULL, "
user_details	+= "PRIMARY KEY(Id), "
user_details	+= "FOREIGN KEY(Id) REFERENCES users(Id) ON DELETE CASCADE, "
user_details	+= "Name VARCHAR(255) NOT NULL, "
user_details	+= "Address VARCHAR(512) NOT NULL, "
user_details	+= "Phone VARCHAR(32) NOT NULL)"

user_roles	 = "CREATE TABLE IF NOT EXISTS user_roles ("
user_roles	+= "Id int(11) unsigned NOT NULL, "
user_roles	+= "PRIMARY KEY(Id), "
user_roles	+= "FOREIGN KEY(Id) REFERENCES users(Id) ON DELETE CASCADE, "
user_roles	+= "User BOOLEAN DEFAULT false, "
user_roles	+= "Rewarder BOOLEAN DEFAULT false, "
user_roles	+= "UrbanPlanner BOOLEAN DEFAULT false, "
user_roles	+= "Admin BOOLEAN DEFAULT false)"

activation	 = "CREATE TABLE IF NOT EXISTS activation (" 
activation	+= "Id int(11) unsigned NOT NULL, "
activation	+= "PRIMARY KEY(Id), "
activation	+= "FOREIGN KEY(Id) REFERENCES users(Id) ON DELETE CASCADE, "
activation	+= "Token VARCHAR(128) NOT NULL UNIQUE, "
activation	+= "Expiration TIMESTAMP NOT NULL)"

user_sessions	 = "CREATE TABLE IF NOT EXISTS sessions ("
user_sessions	+= "Session VARCHAR(255) NOT NULL UNIQUE PRIMARY KEY, "
user_sessions	+= "UserId	INT(11) UNSIGNED NOT NULL, "
user_sessions	+= "FOREIGN KEY(UserId) REFERENCES users(Id) ON DELETE CASCADE, "
user_sessions	+= "CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)"


print "... creating all the users tables ..."
cursor.execute(users)
cursor.execute(user_details)
cursor.execute(user_roles)
cursor.execute(activation)
cursor.execute(user_sessions)

mariadb_connection.commit()
mariadb_connection.close()
print '... Setup finished. Exiting now.'
