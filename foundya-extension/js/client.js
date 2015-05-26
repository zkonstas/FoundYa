var serverUrl = "http://foundyaapp-env.elasticbeanstalk.com/chat";
var userId;
var taxonomy;
var chatroom;
var url;
var spinner;


var main = function() {

	$("#chatMessage").prop('disabled', true);

	var target = document.getElementById('chatContainer');
	spinner = new Spinner().spin(target);

	// localStorage.setItem("id", -1);

	chrome.tabs.query({'active': true, 'lastFocusedWindow': true}, function (tabs) {
    	url = tabs[0].url;
    	authenticateUser(url);
	});

	$("#chatMessage").keypress(function(e) {
    	if(e.which == 13) {
    		var text = this.value;

    		if(text!=="") {
    			$("#chatMessage").val("");
    			sendMessage(text);
    			var message = {
    				"userId": userId,
    				"Url": url,
    				"Message": text,
    				"MessageTimestamp": $.format.date(new Date().getTime(), "HH:mm")
    			}
    			displayMessage(message);
    		}
    	}
	});

	$("#btn-chat").click(function() {
		var text = $("#chatMessage").val();

		if(text!=="") {
			$("#chatMessage").val("");
			sendMessage(text);
			var message = {
				"userId": userId,
				"Url": url,
				"Message": text,
				"MessageTimestamp": $.format.date(new Date().getTime(), "HH:mm")
			}
			displayMessage(message);
    	}
	});
}

var sendMessage = function(message) {

	var sendMessageData = {
		"type": "sendMessage",
		"userId": userId,
		"url": url,
		"chatroom": chatroom,
		"message": message
	};

	$.ajax({
		type : "POST",
	    url : serverUrl,
	    data : sendMessageData,
	    dataType: 'json'
	}).done(function( response ) {
		console.log(response);
		if(response.status==="ok") {
	    	console.log("Message sent successfully!");
	    }
	    else {
	    	console.log("Server did not successfully process message!");
	    }
	    // console.log(response.status);
	});
}

var getMessages = function() { 

	var getMessagesData = {
		"type": "getMessages",
		"userId": userId,
		"chatroom": chatroom,
		"url": url,
	};

	$.ajax({
		type : "POST",
	    url : serverUrl,
	    data : getMessagesData,
	    dataType: 'json'
	}).done(function( response ) {
		if(response.status==="ok") {
	    	console.log("Messages received successfully!");
	    	console.log(response.messages);

	    	displayMessages(response.messages);
	    }
	    else {
	    	console.log("Server did not successfully process message!");
	    	console.log(response.status);
	    }
	});
}

var authenticateUser = function(url) {

	userId = localStorage.getItem("id");
	// userId= 82;

	if(userId==null || userId==-1) {

		$.ajax({
			type : "GET",
		    url : serverUrl,
		    data : { "req": "signup" },
		    dataType: 'text'
		}).done( function(id) {
			userId = id;
			localStorage.setItem("id", userId);
			console.log("Saved User Id: "+userId);

			loginToChatRoom(userId, url);

		});
	}
	else {
		console.log("User signed in with UserId: "+userId);
		loginToChatRoom(userId, url);
	}

}

var loginToChatRoom = function(userId, url) {
	// console.log(url);

	var loginData = {
		"type": "loginToChatRoom",
		"userId": userId,
		"url": url,
	};

	$.ajax({
		type : "POST",
	    url : serverUrl,
	    data : loginData,
	    dataType: 'json'
	}).done(function( response ) {
		console.log(response);
		if(response.status==="ok") {
			console.log("User with id: "+userId +" just logged in!");

			spinner.stop();

			taxonomy = response.taxonomy;
			chatroom = response.table;

			$("#chat_room").text(taxonomy);
			$("#chatMessage").prop('disabled', false);
			
			getMessages();
			setInterval(getMessages, 2000);
		}
		else {
	    	console.log("Could not log into chatroom!");
	    	console.log(response.status);
	    }
	});
}

var displayMessages = function(messages) {

	$.each(messages, function(index, message) {
		message.MessageTimestamp = $.format.date(message.MessageTimestamp*1000, "HH:mm");
		displayMessage(message);
	});

}

// var displayMessage = function(message) {

// 		var usr_msg="";
// 		usr_msg += "<div class=\"user_msg\">";
// 		usr_msg += "";
// 		usr_msg += "<div class=\"user\">";
// 		usr_msg += "<div class=\"username\">";
// 		usr_msg += "User-"+message.userId;
// 		usr_msg += "<\/div>";
// 		usr_msg += "<div class=\"url\">";
// 		usr_msg += " ("+message.Url+")";
// 		usr_msg += "<\/div>";
// 		usr_msg += "<\/div>";
// 		usr_msg += "";
// 		usr_msg += "<div class=\"msg\">";
// 		usr_msg += "<div class=\"message\">";
// 		usr_msg += message.Message;
// 		usr_msg += "<\/div>";
// 		usr_msg += "<div class=\"time\">";
// 		usr_msg += " ("+$.format.date(message.MessageTimestamp*1000, "HH:mm")+")";
// 		usr_msg += "<\/div>";
// 		usr_msg += "<\/div>";
// 		usr_msg += "";
// 		usr_msg += "<\/div>";

// 		$("#chatContainer").append(usr_msg);
		
// 		$("#chatContainer").animate({
// 			scrollTop: $("#chatContainer").prop("scrollHeight")
//     	}, 50);
// }

var displayMessage = function(message) {
	
	var usr_msg="";
	usr_msg += "<li class=\"clearfix\">";
	usr_msg += "<div class=\"chat-body clearfix\">";
	usr_msg += "<div class=\"header\">";
	usr_msg += "<strong class=\"primary-font\">User-"+message.userId+"<\/strong>";
	usr_msg += "<small class=\"pull-right text-muted\">";
	usr_msg += "<span class=\"glyphicon glyphicon-time\"><\/span>"+message.MessageTimestamp+"<\/small>";
	usr_msg += "<br\/><p class=\"pull-right\"><em class=\"url\">"+message.Url+"<\/em><\/p>";
	usr_msg += "<\/div>";
	usr_msg += "<br\/><p>"+message.Message+"<\/p>";
	usr_msg += "<\/div>";
	usr_msg += "<\/li>";

	$("#chatMessages").append(usr_msg);

	$("#chatContainer").animate({
			scrollTop: $("#chatContainer").prop("scrollHeight")
    }, 50);
}

var opts = {
  lines: 13, // The number of lines to draw
  length: 20, // The length of each line
  width: 10, // The line thickness
  radius: 30, // The radius of the inner circle
  corners: 1, // Corner roundness (0..1)
  rotate: 0, // The rotation offset
  direction: 1, // 1: clockwise, -1: counterclockwise
  color: '#000', // #rgb or #rrggbb or array of colors
  speed: 0.8, // Rounds per second
  trail: 85, // Afterglow percentage
  shadow: false, // Whether to render a shadow
  hwaccel: false, // Whether to use hardware acceleration
  className: 'spinner', // The CSS class to assign to the spinner
  zIndex: 2e9, // The z-index (defaults to 2000000000)
  top: '50%', // Top position relative to parent
  left: '50%' // Left position relative to parent
};

$(document).ready(main);

// var myIP = function() {
//     if (window.XMLHttpRequest) xmlhttp = new XMLHttpRequest();
//     else xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
  
//     xmlhttp.open("GET"," http://api.hostip.info/get_html.php ",false);
//     xmlhttp.send();
  
//     hostipInfo = xmlhttp.responseText.split("n");
  
//     for (i=0; hostipInfo.length >= i; i++) {
//         ipAddress = hostipInfo[i].split(":");
//         if ( ipAddress[0] == "IP" ) return ipAddress[1];
//     }
  
//     return false;
// }