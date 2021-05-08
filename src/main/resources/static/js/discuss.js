// 一开始就为publishBtn绑定publish方法
$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});


function like(btn, entityType, entityId, entityAuthorId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityAuthorId": entityAuthorId, "postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? "已赞" : "赞");
            } else {
                alert(data.msg);
                window.location.href = CONTEXT_PATH + "/login";
            }
        }
    );
}

// 置顶操作
function setTop() {
    var postId = $("#postId").val();

    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#topBtn").attr("disabled", "disabled");
                // $("#deleteBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    )

}

// 加精操作
function setWonderful() {
    var postId = $("#postId").val();

    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
                // $("#deleteBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    )

}

// 删除操作
function setDelete() {
    var postId = $("#postId").val();

    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    )

}