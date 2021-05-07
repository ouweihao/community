// 一开始就为publishBtn绑定publish方法
$(function(){
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");

    // 发送AJAX请求之前，将CSRF令牌设置到请求头中
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e, xhr, options) {
    //     // 发请求时就会携带防csrf攻击的数据
    //     xhr.setRequestHeader(header, token);
    // });

    // 得到title和content
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    // 使用ajax异步添加帖子
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content},
        function (data) {
            data = $.parseJSON(data);
            // 在提示框中返回消息
            $("#hintBody").text(data.msg);
            // 显示提示框
            $("#hintModal").modal("show");
            // 2秒后,自动隐藏提示框
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 刷新页面
                if (data.code == 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    )

}