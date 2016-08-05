sudo rm -R /var/lib/tomcat7/webapps/TRACEStore*
mvn clean compile package
cp ./target/TRACEStore.war /var/lib/tomcat7/webapps
echo "TRACEStore has been deployed"
