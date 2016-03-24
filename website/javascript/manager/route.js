$(document).ready(function(){
    // jQuery methods go here...
    var urlString = 'http://146.193.41.50:8080/trace/tracker/';
    var route = 'route/';
    var username = document.getElementById("username").getAttribute("data");
    var data = document.getElementById("map").getAttribute("data");
    var response = jQuery.parseJSON(data);
    var startingLatitude = 38.733535766602;
    var startingLongitude = -9.1371507644653;

    if(response.length > 0){
        startingLatitude = response[0].latitude;
        startingLongitude = response[0].longitude;
    }

    //	alert(startingLatitude);

    // Leaflet map initialization	  
    var map = L.map('map').setView([startingLatitude, startingLongitude], 15); // Centering
    //    var map = L.map('map').setView([38.733535766602, 	-9.1371507644653], 15); // Centering
    //    var map = L.map('map').setView([38.70695, -9.13513], 12); // Centering
    //    var map = L.map('map').setView([startingLongitude, startingLatitude], 5); // Centering
    //    var mapQuestAttr = 'Tiles Courtesy of <a href="http://www.mapquest.com/">MapQuest</a> &mdash; ';
    var mapQuestAttr = '';
    //    var osmDataAttr = 'Map data &copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
    var osmDataAttr = '';
    var mopt = {
        url: 'http://otile{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.jpeg',
        options: {attribution:mapQuestAttr + osmDataAttr, subdomains:'1234'}
    };
    var osm = L.tileLayer("http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
    var mq = L.tileLayer(mopt.url,mopt.options);
    mq.addTo(map);
    //map.locate({setView: true, maxZoom: 9});
    var baseMaps = {
        "Map Quest": mq,
        "Open Street Map":osm
    };

    //    alert(response[0].latitude);

    // parse points in array
    var parsedArray = jQuery.parseJSON(data);
    var trajectory = [];
    for(var i = 0; i < parsedArray.length; i++) {
        var point = [parsedArray[i].latitude, parsedArray[i].longitude];
        trajectory[i] = point;
    }

    //     add trajectory to map
    if(parsedArray.length > 1) {
        console.log(data);
        L.polyline(trajectory, {
            color: 'red',
            weight: 4,
            opacity: 1
        }).addTo(map);
    }else if(parsedArray.length > 0){
        L.marker(trajectory[0]).addTo(map)
            .bindPopup('Single Point Trajectory.')
            .openPopup();
    };
});





//    $.ajax({
//        type: 'GET',
//        url: urlString + route + username,
//        datatype: 'json',
//        success: function(data) {
//            document.getElementById("userSessionsHeader").innerHTML = "User Sessions: " + data.length;
//
//            for(x in data){
//                table += "<tr><td><a id='route' href='/manager/route.php'>" + data[x] + "</a></td></tr>";
//            };
//            document.getElementById("traceSessions").innerHTML = table;
//        }
//    });

//    var mymap = L.map('mapid').setView([51.505, -0.09], 13);
////
//    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
//        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
//        maxZoom: 18,
//        id: 'your.mapbox.project.id',
//        accessToken: 'your.mapbox.public.access.token'
//    }).addTo(mymap);