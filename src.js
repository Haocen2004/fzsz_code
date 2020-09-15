function auto_select(is_v2){
	var as_ts,as_send,as_goal_ts;
	as_send = false;
	as_goal_ts = 1600178400000;
	while(!as_send){
		as_ts = (new Date()).valueOf();
		if ( as_ts >= as_goal_ts ){
			if(is_v2){
				var s1=$('#as_s1').val(),s2=$('#as_s2').val();
				Ajax('single_submit',s1);
				Ajax('single_submit',s2);
			} else {
				Ajax('submit');
			}
			console.log("Request send succeed!");
			console.log("Send as "+as_ts);
			as_send = true;
			mui.toast("抢课请求发送成功，将在5秒后刷新页面");
			setTimeout("window.location.reload()","5000");
		}
	}
}

function main(){
	if (document.URL != 'http://szxk.fjyun.net/szxk/detail2.html') {
		alert("请在正确界面注入！")
		setTimeout("location.replace('http://szxk.fjyun.net/szxk/detail2.html')",3000);
		return;
	}
    if(!confirm("感谢您使用自动抢课脚本\n\n当前版本为测试版,不保证抢课成功率\n请勿滥用并承诺自愿承担一切后果！\nPowered By Hao_cen")){
        return;
	}
	
    var c1=0,c2,as_credit,is_v2 = false,name = new Array(),uid = new Array();
    $("tr").each(function(){
        if($(this).attr('obj-id') != undefined){

            c2=0;
            uid[c1] = $(this).attr('obj-id');
            $(this).children().toArray().forEach(element => {
                c2 = c2 + 1;
                if (c2 == 1){
                    name[c1] = element.innerHTML;
				}
				if (c2 == 2){
                    as_credit = parseInt(element.innerHTML);
                }
            });
            if (as_credit == 1 && is_v2 == false) {
                is_v2 = true;
            }
            c1 = c1 + 1;
        }
    });
    if (is_v2){
        
        mui.toast("检测到V2型选课系统\n请在修改后的网页上选课提交\n等待期间网页缓慢或未响应为正常现象");
        console.log("found v2");
        var div = '<div id="as_div" style="display: block;position:fixed;z-index:9999;background-color:#66ccff;text-align:center;font-color:black;"><p style="font-size:x-large;">选择课程时请注意课程备注！<br>不支持抢2节相同课程！<br>如有需要请点击切换按钮！</p><br><p>课程一</p><select id="as_s1">' ;
        var selector = "";
        for (let i = 0; i < name.length; i++) {
            selector = selector + "<option value="+uid[i]+">"+name[i]+"</option>";
        }
        selector = selector + "</select>";
        div =div +selector + '<br><p>课程二</p><select id="as_s2">' + selector+ '<br><button id="as_submit">提交</button><button id="as_switch">切换模式</button></div>';
        $("body").append(div); 
		$("table").hide();
		$("#as_switch").click(function(){
			$("#as_div").hide(); 
			$("table").show();
			$(".foot").off("click",".do-submit");
			console.log("switch to v1");
			mui.toast("已切换至V1选课\n请选定要抢的课程然后按下确认选课按钮\n系统将自动接管抢课\n等待期间网页缓慢或未响应为正常现象");
			$('.do-submit').click(function(){
				mui.toast("已确认请求,正在等待选课系统开放");
				console.log("已确认请求,正在等待选课系统开放");
				setTimeout("auto_select(false)","3000");
			});
		});

        $("#as_submit").click(function(){
			$(this).attr('disabled',true);
			$('#as_switch').attr('disabled',true);
            mui.toast("已确认请求,正在等待选课系统开放");
            console.log("已确认请求,正在等待选课系统开放");
			setTimeout("auto_select(true)","3000");
        });
    } else {
        $(".foot").off("click",".do-submit");
        console.log("found v1");
        mui.toast("检测到V1型选课系统\n请选定要抢的课程然后按下确认选课按钮\n系统将自动接管抢课\n等待期间网页缓慢或未响应为正常现象");
        $('.do-submit').click(function(){
            mui.toast("已确认请求,正在等待选课系统开放");
            console.log("已确认请求,正在等待选课系统开放");
			setTimeout("auto_select(false)","3000");
        });
    }
}
main()