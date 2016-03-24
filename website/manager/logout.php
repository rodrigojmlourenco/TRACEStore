<!DOCTYPE html>
<html lang="en">
    <head>
        <link rel="import" href="/header/header.html">
        <!--		<script src="/javascript/events.js" type="text/javascript"></script>-->
        <?php include( $_SERVER['DOCUMENT_ROOT'] . '/rest/restClient.php' ); ?>

        <?php
        session_start();
        $_SESSION["username"]="";
        session_unset(); 
        session_destroy();

        header("Location: /index.php");
        die();
        ?>
    </head>
</html>