
function hasOneUppercase(str) {
    return (/[A-Z]/.test(str));
}

function hasOneLowercase(str) {
    return (/[a-z]/.test(str));
}

function hasOneNumber(str) {
    return (/\d/.test(str));
}

//*.,
function hasOneSpecial(str) {
    return (/\-|\+|\!|\#|\_|\@|\$|\%|\^|\&|\?|\,|\.|\*/.test(str));
}

function acceptableUsername(username) {
    var length = username.length;

    return ((length < 16 ) && (length > 4));
}

function acceptablePhoneNumber(number) {
    var length = number.length;
    var isNumber = !isNaN(number);

    return ((length < 16 ) && (length > 8) && isNumber);
}

function acceptablePassword(password) {
    var length = password.length;
    var hasUpper = hasOneUppercase(password);
    var hasLower = hasOneLowercase(password);
    var hasNumber = hasOneNumber(password);
    var hasSpecial = hasOneSpecial(password);

    return ((length < 26 ) && (length > 7) && hasUpper && hasLower && hasNumber && hasSpecial);
}



$(document).ready(function(){
    // jQuery methods go here...
    var urlString = 'http://146.193.41.50:8080/trace/';

    //	$("#button1").click(function(){
    //		$.ajax({
    //			type: 'GET',
    //			url: urlString + sessions + 'miguel',
    //			datatype: 'json',
    //			success: function(data) {
    //				for(x in data){
    //					sessionsText+=data[x] + " ";
    //				};
    //				alert(sessionsText);
    //			}
    //		});
    //	});

    //        /*$.ajax({
    //        url: 'http://146.193.41.50:8080/trace/tracker/route/0.2958656155933016',
    //        type: 'GET',
    //        data: '', // or $('#myform').serializeArray()
    //        success: function() { alert('PUT completed'); }
    //    });*/
    //
    //        /*$.ajax({
    //        url: 'http://example.com/',
    //        type: 'PUT',
    //        data: 'ID=1&Name=John&Age=10', // or $('#myform').serializeArray()
    //        success: function() { alert('PUT completed'); }
    //    });*/

    //	$('#RegisterButton').click(function(){
    //		$('#RegisterButton').submit();
    //	});

    //	TODO Validar os campos
    $('#registerForm').submit(function(){
        //		alert("abc");
        var req = {};

        req.name = $('#inputName').val();
        req.username = $('#inputUsername').val();
        req.email = $('#inputEmail').val();
        req.password = $('#inputPassword').val();
        req.confirm = $('#inputConfirm').val();
        req.phone = $('#inputPhone').val();
        req.address = $('#inputAddress').val();

        //        PARSING SECTION
        //        CHECK IF USERNAME IS ACCEPTABLE
        if(!acceptableUsername(req.username)){
            alert("This username is not acceptable. Make sure it has a length between 8 and 15 characters.");
            return false;
        }

        //        CHECK IF PHONE NUMBER IS ACCEPTABLE
        if(!acceptablePhoneNumber(req.phone)){
            alert("This phone number is not acceptable. Make sure your typed only numbers with a total length between 12 and 15.");
            return false;
        }

        //        CHECK IF PASSWORD IS ACCEPTABLE
        if(!acceptablePassword(req.password)){
            alert("This password is not acceptable. Make sure your password is between 8 and 25 characters, with at least one uppercase, one lowercase, one number and one special character such as #");
            return false;
        }

        var dataForRegister = JSON.stringify(req);

        console.log("dataForRegister: " + dataForRegister);

        $.ajax({
            method: 'POST',
            url: urlString + 'tracker/register',
            datatype: 'json',
            data: dataForRegister,
            contentType: 'application/json',
            success: function(data) {
                console.log("First Ajax return: " + data);

                //				TODO: GET EMAILS WORKING
                if(data!=null){
                    $.ajax({
                        method: 'POST',
                        url: urlString + 'auth/activate?token=' + data,
                        success: function(data) {
                            if(data!=null && data.success){
                                alert("User Successfully registered!");
                                window.location.replace("http://trace.noip.me/signin.php");
                            }else{
                                alert("User Registry Failed!");
                            }
                            console.log("Second Ajax return:" + JSON.stringify(data));
                        }
                    });
                }
            }
        });
        return false;
    });

    $('#signinForm').submit(function(){

        var username = $('#inputUsername').val();
        var password = $('#inputPassword').val();

        var dataForSignin = "username=" + encodeURIComponent(username) + "&password=" + encodeURIComponent(password);

        console.log("data: " + dataForSignin);

        $.ajax({
            method: 'POST',
            url: urlString + 'auth/login',
            datatype: 'json',
            data: dataForSignin,
            contentType: 'application/x-www-form-urlencoded',
            success: function(data) {
                console.log("First Ajax return: " + data);
                data=jQuery.parseJSON(data);
                if(data==null || !data.success){
                    window.location.replace("http://trace.noip.me/signin.php");
                }
            }
        });

	function validateFederatedToken(token){
	$.ajax({
            method: 'POST',
            url: 'http://146.193.41.50:8081/trace/auth/login',
            data: "token="+token,
            contentType: 'application/x-www-form-urlencoded',
            success: function(data) {
                console.log("First Ajax return: " + data);
                data=jQuery.parseJSON(data);
                if(data==null || !data.success){
                    window.location.replace("http://trace.noip.me/signin.php");
                }
            }
        });

	}
    });
});


