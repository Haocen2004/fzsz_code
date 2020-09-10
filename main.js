function auto_select(){
	var as_ts,as_send,as_goal_ts;
	send = false;
  //这里设定选课系统开放时间戳
	as_goal_ts = 12345678;
	console.log("Waiting for System Open.");
	while(!send){
		as_ts = (new Date()).valueOf();
		if ( as_ts >= as_goal_ts ){
			Ajax("submit")
			console.log("Request send succeed!");
			console.log("Send as "+as_ts);
			send = true;
		}
  }
}
