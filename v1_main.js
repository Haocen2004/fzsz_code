function auto_select(){
	if(!confirm("感谢您使用自动抢课脚本\n本脚本适配v1抢课系统,表现为选课后需手动点击提交\n\n当前版本为测试版,不保证抢课成功率\n请勿滥用并承诺自愿承担一切后果！\nPowered By Hao_cen")){
        return;
    }
	var as_ts,as_send,as_goal_ts;
	send = false;
  //这里设定选课系统开放时间戳
	as_goal_ts = 12345678;
	console.log("Waiting for System Open.");
	while(!as_send){
		as_ts = (new Date()).valueOf();
		if ( as_ts >= as_goal_ts ){
			Ajax("submit")
			console.log("Request send succeed!");
			console.log("Send as "+as_ts);
			as_send = true;
		}
  }
}
