<!DOCTYPE html>
<html lang="en">
    <head>
        <link rel="import" href="/import/header.html">
        <link rel="import" href="/import/footer.html">
        <!--		<link rel="import" href="/header/restclient.php">-->
        <script src="/javascript/events.js" type="text/javascript"></script>
        <?php 
        //		include( $_SERVER['DOCUMENT_ROOT'] . '/rest/restClient.php' ); 
        ?>
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
                        <li><a href="index.php">Home <span class="sr-only">(current)</span></a></li>
                        <li class="active"><a href="#">Register</a></li>
                        <li><a href="signin.php">Sign in</a></li>     
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
        // define variables and set to empty values
        //		$name = $address = $phone = $email = $username = $password = $confirm = "";
        //
        //		if ($_SERVER["REQUEST_METHOD"] == "POST") {
        //			$name = $_POST["inputUsername"];
        //			$address = $_POST["inputPassword"];
        //			$phone = $_POST["inputPassword"];
        //			$email = $_POST["inputPassword"];
        //			$username = $_POST["inputPassword"];
        //			$password = $_POST["inputPassword"];
        //			$confirm = $_POST["inputPassword"];
        //
        //			registerRest($name, $address, $phone, $email, $username, $password, $confirm);
        //			
        //			//						var_dump(signinRest($username,$password));
        //
        //			//			if(registerRest($name, $address, $phone, $email, $username, $password, $confirm)){
        //			////				echo "<script> alert(\"Sucessfully Registered User " . $username . ".\"); </script>";
        //			//
        //			////				$_SESSION["username"] = $username;
        //			////				$_SESSION["password"] = $password;
        //			////				header("Location: /manager/sessions.php");
        //			////				die();
        //			//			}else{
        //			////				echo "<script> alert(\"Failed to register new account.\"); </script>";
        //			//			}
        //		}
        ?>
    </head>

    <body>

        <form class="form-signin" id="registerForm" action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]);?>" method="post">
            <!--		<form class="form-signin" id="registerForm">-->
            <h2 class="form-signin-heading">Register New Account </h2>

            <p class="lead">
                Name:
                <label for="inputName" class="sr-only">Name</label>
                <input type="text" id="inputName" name="inputName" class="form-control" placeholder="Name" required autofocus>
            </p>

            <p class="lead">
                Local Address:
                <label for="inputAddress" class="sr-only">Local Address</label>
                <input type="text" id="inputAddress" name="inputAddress" class="form-control" placeholder="Local Address" required autofocus>
            </p>

            <p class="lead">
                Phone Number:
                <label for="inputPhone" class="sr-only">Phone Number</label>
                <input type="text" id="inputPhone" name="inputPhone" class="form-control" placeholder="Phone Number" required autofocus>
            </p>

            <p class="lead">
                Email Address:
                <label for="inputEmail" class="sr-only">Email Address</label>
                <input type="email" id="inputEmail" name="inputEmail" class="form-control" placeholder="Email Address" required autofocus>
            </p>

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

            <p class="lead">
                Confirm Password:
                <label for="inputConfirm" class="sr-only">Confirm Password</label>
                <input type="password" id="inputConfirm" name="inputConfirm" class="form-control" placeholder="Confirm Password" required>
            </p>

            <button class="btn btn-lg btn-primary btn-block" type="submit" id="registerButton">Register</button>
        </form>
    </body>
</html>
