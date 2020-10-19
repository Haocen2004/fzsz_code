//宪法知识速答暂存，以后可能会考虑改库名称
//url http://qspfw.moe.gov.cn
while(questionBank_no < questionBankList_size){
    $(".item").each(function(){
        if($(this).data().check == questionBank_answer){
            $(this).click();
            nextQuestion();
        }
    });
}