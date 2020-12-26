//网络评教一键勾选,其他意见/建议需自己填
$('input').each(function(){
    if($(this).val() == 0001 && $(this).attr("type") == "radio"){
        $(this).click();
    }
});