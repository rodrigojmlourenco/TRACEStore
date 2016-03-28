$(document).ready(function(){
	// jQuery methods go here...
	var urlString = 'http://146.193.41.50:8080/trace/tracker/';
	var sessions = 'sessions/';
	var username = document.getElementById("username").getAttribute("data");
	var table = "";
	var sessionsText = "";

	$.ajax({
		type: 'GET',
		url: urlString + sessions + username,
		datatype: 'json',
		success: function(data) {
			document.getElementById("userSessionsHeader").innerHTML = "User Sessions: " + data.length;

			for(x in data){
				table += "<tr><td><a id='route' href='/manager/route.php'>" + data[x] + "</a></td></tr>";
			};
			document.getElementById("traceSessions").innerHTML = table;
		}
	});
});