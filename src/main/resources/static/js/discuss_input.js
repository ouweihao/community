// 一开始就为publishBtn绑定publish方法
$(function () {
    $("#publish-btn").click(publish);
});

function publish() {
    // $('[name="published"]').val(true);
    // do something...
    // var mdValue = document.getElementById("content").value;
    // var converter = new showdown.Converter();
    // var html = converter.makeHtml(mdValue);
    // // document.getElementById("content").val(html);
    // $("#content").val(html);

    var title = $("input[name='title']").val();
    var htmlContent = $(".editormd-html-textarea").val();

    // $("#content").text(htmlContent);
    //
    // alert(title);
    // alert(htmlContent);

    // $('#discusspost-form').submit();

    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": htmlContent},
        function (data) {
            // data = $.parseJSON(data);
            // // 在提示框中返回消息
            // $("#hintBody").text(data.msg);
            // // 显示提示框
            // $("#hintModal").modal("show");
            // // 2秒后,自动隐藏提示框
            // setTimeout(function () {
            //     $("#hintModal").modal("hide");
            //     // 刷新页面
            //     if (data.code == 0) {
            //         window.location.reload();
            //     }
            // }, 2000);
        }
    )

}