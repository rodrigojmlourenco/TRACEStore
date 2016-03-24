<!DOCTYPE html>
<html lang="en">
    <head>
        <link rel="import" href="/import/header.html">
        <link rel="import" href="/import/footer.html">
        <!-- <script src="/javascript/events.js" type="text/javascript"></script>-->
        <?php include( $_SERVER['DOCUMENT_ROOT'] . '/rest/restClient.php' ); ?>
        <nav class="navbar navbar-inverse">
            <div class="container-fluid">
                <!-- Brand and toggle get grouped for better mobile display -->
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="index.php">TRACE</a>
                </div>

                <!-- Collect the nav links, forms, and other content for toggling -->
                <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                    <ul class="nav navbar-nav">
                        <li ><a href="index.php">Home <span class="sr-only">(current)</span></a></li>
                        <li><a href="register.php">Register</a></li>
                        <li class="active"><a href="#">Sign in</a></li>
                        <li><a href="downloads.php">Downloads</a></li>
                    </ul>
                </div><!-- /.navbar-collapse -->
            </div><!-- /.container-fluid -->

        </nav>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
        <meta name="description" content="">
        <meta name="author" content="">
        <link rel="icon" href="../../favicon.ico">

        <title>TRACE Tracking</title>

        <?php
        //        session_unset(); 
        //        session_destroy();
        //        session_start();
        //        $_SESSION["username"]="";

        session_start();

//	if(isset($_POST["token"])){
//		$_SESSION["token"] = $_POST["token"];
//	            header("index.php");
//	            die();
//	}

//	if(isset($_SESSION["token"])){
//            header("Location: /manager/sessions.php");
//           die();
//
//	}else{
//
//	}

        if(isset($_SESSION["username"])){
            header("Location: /manager/sessions.php");
            die();
        }

        //        var_dump(empty($_SESSION["username"]));

        // define variables and set to empty values
        $username = $password = "";

        if ($_SERVER["REQUEST_METHOD"] == "POST") {
            $username = $_POST["inputUsername"];
            $password = $_POST["inputPassword"];

            //			var_dump(signinRest($username,$password));

            if(signinRest($username, $password)){
                session_start();
                $_SESSION["username"] = $username;
                $_SESSION["password"] = $password;
                header("Location: /manager/sessions.php");
                die();
            }else{
                echo "<script> alert(\"Wrong Username or Password.\"); </script>";
            }
        }
        ?>
        <!-- Google Sign-in -->
        <meta name="google-signin-client_id" content="307422557916-mj0ma4qr4fu4tfs9a83f0s2tc8bck847.apps.googleusercontent.com">
        <script src="https://apis.google.com/js/platform.js" async defer></script>
	<script>
		gapi.load('auth2', function(){
			gapi.auth2.init();
		});
	</script>
        
    </head>

    <body>
        <div class="container">
            <!--                        <form class="form-signin" action="/manager/sessions.php" method="post" id="signinForm"> -->
            <form class="form-signin" action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]);?>" method="post" id="signinForm"> 
                <h2 class="form-signin-heading">Please Sign in</h2>
                <p class="lead">
                    Username:
                    <label for="inputUsername" class="sr-only">Username</label>
                    <input type="text" id="inputUsername" name="inputUsername" class="form-control" placeholder="Username" required autofocus>
                </p>

                <p class="lead">
                    Password:
                    <label for="inputPassword" class="sr-only">Password</label>
                    <input type="password" id="inputPassword" name="inputPassword" class="form-control" placeholder="Password" required>
                </p>
                <button id="signin" class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
            </form>

                <!-- Google Sign-in -->
<!--                <div class="g-signin2" data-onsuccess="onSignIn"></div>-->
                <script>

 function validateFederatedToken(token){

        $.ajax({
            method: 'POST',
            url: 'http://146.193.41.50:8081/trace/auth/login',
            data: "token="+token,
	    dataType: 'json',
            contentType: 'application/x-www-form-urlencoded',
            success: function(data) {

		if(data.success){
		console.log('accepted by google');
$('<form action="signin.php" method="POST">' + 
    '<input type="hidden" name="token" value="' + data.token + '">' +
    '</form>').submit();
		}
            }
        });
}



                        function onSignIn(googleUser) {
                           var profile = googleUser.getBasicProfile();
			   var id_token = googleUser.getAuthResponse().id_token;
			  validateFederatedToken(id_token);
                        }

                </script>
        </div><!-- /.container -->
    </body>
</html>
