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
	

print 'Setting up all TRACE\'s rewards databases...'


cmd1 = 'USE '+database;
cursor.execute(cmd1)

# Phase 1 - User Tables
print "Starting Phase 1 - Users' tables"

rewards	 = "CREATE TABLE IF NOT EXISTS rewards ("
rewards	+= "Id int(11) unsigned NOT NULL UNIQUE AUTO_INCREMENT, "
rewards += "PRIMARY KEY(Id), "
rewards	+= "OwnerId INT(11) UNSIGNED NOT NULL, "
rewards += "FOREIGN KEY(OwnerId) REFERENCES users(Id) ON DELETE CASCADE, "
rewards	+= "Conditions VARCHAR(512) NOT NULL, "
rewards	+= "Reward VARCHAR(256) NOT NULL)"

winners	 = "CREATE TABLE IF NOT EXISTS winners ("
winners += "RewardId int(11) unsigned NOT NULL, "
winners	+= "FOREIGN KEY(RewardId) REFERENCES rewards(Id) ON DELETE CASCADE, "
winners	+= "WinnerId int(11) unsigned NOT NULL, "
winners	+= "FOREIGN KEY(WinnerId) REFERENCES users(Id) ON DELETE CASCADE, "
winners += "PRIMARY KEY(RewardId, WinnerId), "
winners += "isNotified TINYINT(1) NOT NULL DEFAULT 0, "
winners += "hasCheckedIn TINYINT(1) NOT NULL DEFAULT 0)"

cursor.execute(rewards)
cursor.execute(winners)

mariadb_connection.commit()
mariadb_connection.close()
print '... Setup finished. Exiting now.'
