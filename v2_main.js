function as_submit(){
    auto_select($('#as_s1').val(),$('#as_s2').val());
}
function auto_select(s1,s2){
	var as_ts,as_send,as_goal_ts;
	send = false;
  //这里设定选课系统开放时间戳，默认2020/9/14 20:00:00
	as_goal_ts = 1600092000000;
	console.log("Waiting for System Open.");
	while(!as_send){
		as_ts = (new Date()).valueOf();
		if ( as_ts >= as_goal_ts ){
            Ajax('single_submit',s1,"3");
            Ajax('single_submit',s2,"4");
			console.log("Request send succeed!");
			console.log("Send as "+as_ts);
			as_send = true;
        }
    }
}
function spawn_selector(){
    if(!confirm("感谢您使用自动抢课脚本\n本脚本适配v2抢课系统,表现为选择时自动提交\n如果要抢单选课程请刷新页面并使用v1抢课脚本\n\n当前版本为测试版,不保证抢课成功率\n请勿滥用并承诺自愿承担一切后果！\nPowered By Hao_cen")){
        return;
    }
    var c1,c2;
    var name = new Array(),uid = new Array();
    c1 = 0
    $("tr").each(function(){
        if($(this).attr('obj-id') != undefined){

            c2=0;
            uid[c1] = $(this).attr('obj-id')
            $(this).children().toArray().forEach(element => {
                c2 = c2 + 1;
                if (c2 == 1){
                    name[c1] = element.innerHTML;
                }
            });
            c1 = c1 + 1;
        }
    });
    var div = '<div style="display: block;position:fixed;z-index:9999;background-color:#66ccff;text-align:center"><p style="font-size:xx-large;">选择课程时请注意课程备注！</p><br><p>周一</p><select id="as_s1">'
    var selector = "";
    for (let i = 0; i < name.length; i++) {
        selector = selector + "<option value="+uid[i]+">"+name[i]+"</option>";
    }
    selector = selector + "</select>";
    div =div +selector + '<br><p>周三</p><select id="as_s2">' + selector+ '<br><button onclick="as_submit()">提交</button></div>'
    $("body").append(div); 
    $("table").hide();
}
spawn_selector()