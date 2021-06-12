$(function () {
    $(".follow-btn").click(follow);
    $(".moderator-btn").click(moderator);
    $(".delete-btn").click(deleteUser);
    $(".forbid-btn").click(forbidUser);
});

function follow() {
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 关注TA

        $.post(
            CONTEXT_PATH + "/follow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        )

        // $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
    } else {
        // 取消关注

        $.post(
            CONTEXT_PATH + "/unfollow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        )

        // $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
    }
}

function moderator() {

    var userId = $("#entityId").val();
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 已经是版主

        $.post(
            CONTEXT_PATH + "/user/ordinary",
            {"userId": userId},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        )
    } else {
        // 不是版主

        $.post(
            CONTEXT_PATH + "/user/moderator",
            {"userId": userId},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        )
    }

}

function deleteUser() {

    var userId = $("#entityId").val();
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 已被踢出

        $.post(
            CONTEXT_PATH + "/user/undelete",
            {"userId": userId},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        )
    } else {
        // 未被踢出

        $.post(
            CONTEXT_PATH + "/user/delete",
            {"userId": userId},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        )
    }

}

function forbidUser() {

    var userId = $("#entityId").val();
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 已被禁言

        $.post(
            CONTEXT_PATH + "/user/unforbid",
            {"userId": userId},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        )
    } else {
        // 未被禁言

        $.post(
            CONTEXT_PATH + "/user/forbid",
            {"userId": userId},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        )
    }

}
