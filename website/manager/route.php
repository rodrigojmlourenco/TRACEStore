<!DOCTYPE html>
<html lang="en">
    <head>
        <link rel="import" href="/import/header.html">
        <link rel="import" href="/import/footer.html">
        <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.6.4/leaflet.css" />
        <script src="http://cdn.leafletjs.com/leaflet-0.6.4/leaflet.js"></script>
        <script src="/javascript/manager/route.js" type="text/javascript"></script>
        <link href="/css/dashboard.css" rel="stylesheet">
        <?php session_start(); ?>

        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
        <meta name="description" content="">
        <meta name="author" content="">
        <link rel="icon" href="../../favicon.ico">



        <?php

        //        var_dump(empty($_SESSION["username"]));

        if(empty($_SESSION["username"])){
            header("Location: /signin.php");
            die();
        }
        ?>

        <!--
<?php
if(isset($_POST["inputUsername"])){
    $_SESSION["username"] = $_POST["inputUsername"];
    $_SESSION["password"] = $_POST["inputPassword"];
}
?>
-->

        <!--		Make it so that javascript can reach the username-->
        <div id="username" data="<?php echo $_SESSION["username"] ?>" />

        <title>TRACE Tracking</title>

        <nav class="navbar navbar-inverse navbar-fixed-top">
            <div class="container-fluid">
                <!-- Brand and toggle get grouped for better mobile display -->
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="/index.php">TRACE Tracking</a>
                </div>

                <!-- Collect the nav links, forms, and other content for toggling -->
                <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                    <ul class="nav navbar-nav navbar-right">
                        <li><a href="/manager/logout.php">Logout</a></li>
                    </ul>
                </div><!-- /.navbar-collapse -->
            </div><!-- /.container-fluid -->
        </nav>

    </head>

    <body>
        <div class="container-fluid">
            <div class="row">
                <div class="col-sm-3 col-md-2 sidebar">
                    <ul class="nav nav-sidebar">
                        <li ><a href="/manager/sessions.php">Sessions <span class="sr-only">(current)</span></a></li>
                        <li class="active" style="padding: 0px 0px 0px 15px"><a href="#"> Route</a></li>
<!--                        <li><a href="#">Statistics</a></li>-->

                        <!--<li><a href="#">Analytics</a></li>
<li><a href="#">Export</a></li>-->
                    </ul>
                    <!--<ul class="nav nav-sidebar">
<li><a href="">Nav item</a></li>
<li><a href="">Nav item again</a></li>
<li><a href="">One more nav</a></li>
<li><a href="">Another nav item</a></li>
<li><a href="">More navigation</a></li>
</ul>
<ul class="nav nav-sidebar">
<li><a href="">Nav item again</a></li>
<li><a href="">One more nav</a></li>
<li><a href="">Another nav item</a></li>
</ul>-->
                </div>
                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
                    <h1 class="page-header">TRACE Tracking Manager - User: <?php echo $_SESSION["username"] ?> </h1>

                    <!--
<div class="row placeholders">
<div class="col-xs-6 col-sm-3 placeholder">
<img src="data:image/gif;base64,R0lGODlhAQABAIAAAHd3dwAAACH5BAAAAAAALAAAAAABAAEAAAICRAEAOw==" width="200" height="200" class="img-responsive" alt="Generic placeholder thumbnail">
<h4>Label</h4>
<span class="text-muted">Something else</span>
</div>
<div class="col-xs-6 col-sm-3 placeholder">
<img src="data:image/gif;base64,R0lGODlhAQABAIAAAHd3dwAAACH5BAAAAAAALAAAAAABAAEAAAICRAEAOw==" width="200" height="200" class="img-responsive" alt="Generic placeholder thumbnail">
<h4>Label</h4>
<span class="text-muted">Something else</span>
</div>
<div class="col-xs-6 col-sm-3 placeholder">
<img src="data:image/gif;base64,R0lGODlhAQABAIAAAHd3dwAAACH5BAAAAAAALAAAAAABAAEAAAICRAEAOw==" width="200" height="200" class="img-responsive" alt="Generic placeholder thumbnail">
<h4>Label</h4>
<span class="text-muted">Something else</span>
</div>
<div class="col-xs-6 col-sm-3 placeholder">
<img src="data:image/gif;base64,R0lGODlhAQABAIAAAHd3dwAAACH5BAAAAAAALAAAAAABAAEAAAICRAEAOw==" width="200" height="200" class="img-responsive" alt="Generic placeholder thumbnail">
<h4>Label</h4>
<span class="text-muted">Something else</span>
</div>
</div>
-->

                    <?php 
    $url = "http://146.193.41.50:8080/trace/tracker/route/" .  $_GET["sessionID"];
             //   "08d56efea9de8d4dddf55748481f4a4080de52542c1b3de307c7c60d53caf998"; 
             $response = file_get_contents($url);
             $result = json_decode($response);
             echo "<h2 class=\"sub-header\" id=\"userRouteHeader\">Route ID: " . $_GET["sessionID"] . "</h2>";
             echo "<h2 class=\"sub-header\" id=\"userRouteHeader\">Route Total Points: " . count($result) . "</h2>";  

                    ?>
                    <!--                    <h2 class="sub-header" id="userSessionsHeader">Route:</h2>-->
                    <?php 
                    echo "<div id=\"map\" style=\" height: 360px; \" data=$response></div>";
                    ?>
                    <div class="table-responsive">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Latitude</th>
                                    <th>Longitude</th>
                                    <!--									<th>Date</th>-->
                                    <!--									<th>Route</th>-->
                                </tr>
                            </thead>
                            <tbody id="traceSessions">
                                <?php 
                                foreach($result as $res){
                                    echo "<tr><td>$res->name</td><td>$res->latitude</td><td>$res->longitude</td></tr>";
                                }
                                ?>
                            </tbody>
                        </table>
                    </div>



                    <!--                    <div id="mapid" style=" height: 360px; "></div>-->
                </div>
            </div>
        </div>
    </body>
</html>