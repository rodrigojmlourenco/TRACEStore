<!DOCTYPE html>
<html lang="en">
    <head>
        <link rel="import" href="/import/header.html">
        <link rel="import" href="/import/footer.html">
        <!--		<script src="/javascript/manager/sessions.js" type="text/javascript"></script>-->
        <link href="/css/dashboard.css" rel="stylesheet">
        <?php include( $_SERVER['DOCUMENT_ROOT'] . '/rest/restClient.php' ); ?>
        <?php ini_set('display_errors', 'On'); ?>
        <?php session_start(); ?>

        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
        <meta name="description" content="">
        <meta name="author" content="">
        <link rel="icon" href="../../favicon.ico">

        <!--		"username=" + encodeURIComponent(username) + "&password=" + encodeURIComponent(password);-->
        <?php

        //        var_dump(empty($_SESSION["username"]));

        if(empty($_SESSION["username"])){
            header("Location: /signin.php");
            die();
        }
        ?>

        <!--		Make it so that javascript can reach the username-->
        <div id="username" data="<?php echo $_SESSION["username"] ?>" />

        <!--
DESTROY SESSION CODE - commented for now, must be used in the logout link
<?php
    // remove all session variables
    //	session_unset(); 

    // destroy the session 
    //			 session_destroy(); 
?>
-->

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
                        <li class="active"><a href="#">Sessions <span class="sr-only">(current)</span></a></li>
                        <!--						<li><a href="#">Route</a></li>-->
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
    //DEBUG
    //	ini_set('display_errors', 'On');

    $api = new RestClient(array(
        'base_url' => "http://146.193.41.50:8080/trace", 
    ));
             $result = $api->get("tracker/sessions/" . $_SESSION["username"])->decode_response();
             echo "<h2 class=\"sub-header\" id=\"userSessionsHeader\">User Sessions: " . count($result) . "</h2>"
                    ?>

                    <div class="table-responsive">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>Sessions IDs</th>
                                    <th>Time and Date</th>
                                    <!--									<th>Date</th>-->
                                    <!--									<th>Route</th>-->
                                </tr>
                            </thead>
                            <tbody id="traceSessions">

                                <?php 
                 
             for ($x = 0; $x < count($result); $x++) {
                 $object = json_decode($result[$x]);
                 echo "<tr><td><a id='route' href='/manager/route.php?sessionID=" . $object->session . "'>" . $object->session . "</a></td><td>" . $object->date . "</td></tr>";
             }

             //                 for ($x = 0; $x < count($result); $x+=2) {
             //					 $y=$x+1;
             //					 echo "<tr><td><a id='route' href='/manager/route.php?sessionID=$result[$x]'>$result[$x]</a></td><td>$result[$y]</td></tr>";
             //				 } 

             //			 foreach($result as $res){
             //				 echo "<tr><td><a id='route' href='/manager/route.php?sessionID=$res'>$res</a></td><td>09/03/2016</td></tr>";
             //			 }

             //			 echo "<td>" + $response.size(); 
                                ?>



                                <tr>
                                    <!--
<td>09/02/2016</td>
<td>A->B->C</td>
-->
                                    <!--
<td>odio</td>
<td>Praesent</td>
-->
                                </tr>
                                <tr>
                                    <!--
<td>08/02/2016</td>
<td>A->B->C</td>
-->
                                    <!--
<td>odio</td>
<td>Praesent</td>
-->
                                </tr>
                                <!--
<tr>
<td>1,004</td>
<td>dapibus</td>
<td>diam</td>
<td>Sed</td>
<td>nisi</td>
</tr>
<tr>
<td>1,005</td>
<td>Nulla</td>
<td>quis</td>
<td>sem</td>
<td>at</td>
</tr>
<tr>
<td>1,006</td>
<td>nibh</td>
<td>elementum</td>
<td>imperdiet</td>
<td>Duis</td>
</tr>
<tr>
<td>1,007</td>
<td>sagittis</td>
<td>ipsum</td>
<td>Praesent</td>
<td>mauris</td>
</tr>
<tr>
<td>1,008</td>
<td>Fusce</td>
<td>nec</td>
<td>tellus</td>
<td>sed</td>
</tr>
<tr>
<td>1,009</td>
<td>augue</td>
<td>semper</td>
<td>porta</td>
<td>Mauris</td>
</tr>
<tr>
<td>1,010</td>
<td>massa</td>
<td>Vestibulum</td>
<td>lacinia</td>
<td>arcu</td>
</tr>
<tr>
<td>1,011</td>
<td>eget</td>
<td>nulla</td>
<td>Class</td>
<td>aptent</td>
</tr>
<tr>
<td>1,012</td>
<td>taciti</td>
<td>sociosqu</td>
<td>ad</td>
<td>litora</td>
</tr>
<tr>
<td>1,013</td>
<td>torquent</td>
<td>per</td>
<td>conubia</td>
<td>nostra</td>
</tr>
<tr>
<td>1,014</td>
<td>per</td>
<td>inceptos</td>
<td>himenaeos</td>
<td>Curabitur</td>
</tr>
<tr>
<td>1,015</td>
<td>sodales</td>
<td>ligula</td>
<td>in</td>
<td>libero</td>
</tr>
-->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>