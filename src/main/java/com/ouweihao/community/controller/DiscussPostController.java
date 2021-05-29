package com.ouweihao.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.ouweihao.community.entity.*;
import com.ouweihao.community.event.EventProducer;
import com.ouweihao.community.service.*;
import com.ouweihao.community.util.CommunityConstant;
import com.ouweihao.community.util.CommunityUtil;
import com.ouweihao.community.util.HostHolder;
import com.ouweihao.community.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscussPostController.class);

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.Imgupload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/add", method = RequestMethod.GET)
    public String getAddPostPage(Model model) {

        // 得到所有的版块
        List<Section> allSections = sectionService.getAllSections();
        model.addAttribute("sections", allSections);

        return "/site/discusspost_input";
    }

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public String addDiscussPost(String title, @RequestParam(name = "my-editormd-markdown-doc") String mdContent,
                                 @RequestParam(name = "my-editormd-html-code") String[] htmlContent,
                                 HttpServletRequest request,
                                 int sectionId, RedirectAttributes attributes) {

        User currentUser = hostHolder.getUser();

        // 若未登陆则无法发布帖子
        if (currentUser == null) {
            return CommunityUtil.getJsonString(403, "您还未登陆哦！！");
        }

//        System.out.println(htmlContent[1]);
//        System.out.println(mdContent);

        String commentable = request.getParameter("commentable");
        System.out.println("commentable: " + commentable);

        DiscussPost post = new DiscussPost();

        post.setUserId(currentUser.getId());
        post.setTitle(title);
        post.setMdcontent(mdContent);
        post.setHtmlcontent(htmlContent[1]);
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        post.setCommentable(Integer.valueOf(commentable));
        post.setSectionId(sectionId);
        post.setViews(0);
        discussPostService.addDiscussPost(post);
        elasticSearchService.saveDiscussPost(post);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_POST)
                .setEntityId(post.getId())
                .setAuthorId(currentUser.getId());

        eventProducer.fireEvent(event);

        // 计算帖子分数
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, post.getId());

        attributes.addFlashAttribute("publishstatus", 1);
        attributes.addFlashAttribute("publishmsg", "发布帖子成功！");

        // 后面将统一处理异常
        return "redirect:/index";

//        return CommunityUtil.getJsonString(0, "发布成功！");
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject editormdPic(@RequestParam(value = "editormd-image-file", required = true) MultipartFile file) throws Exception {

        // 得到初始文件名
        String fileName = file.getOriginalFilename();

        String suffix = fileName.substring(fileName.lastIndexOf("."));

        fileName = CommunityUtil.generateUUID() + suffix;

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存在的路径
        File dest = new File(uploadPath + "/" + fileName);

//        File targetFile = new File(uploadPath, fileName);
//        if(!targetFile.exists()){
//            targetFile.mkdirs();
//        }

        System.out.println(uploadPath);

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败！服务器发生异常！", e);
        }

        System.out.println(domain);

        // 访问路径  http://localhost:8080/community/discuss/EditorImage/xxx.png
        // 访问路径  http://localhost:8080/community/img/upload/xxx.png
        JSONObject res = new JSONObject();
        res.put("url", domain + contextPath + "/discuss/EditorImage/" + fileName);
        System.out.println(domain + contextPath + "/discuss/uploadEditorImage/" + fileName);
//        System.out.println(res.get("url"));
        res.put("success", 1);
        res.put("message", "upload success!");

        return res;

    }

    @RequestMapping(path = "/EditorImage/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放文件的路径
        fileName = uploadPath + "/" + fileName;

        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf('.'));

        // 响应图片
        response.setContentType("image/" + suffix);

        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0; // offset
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (Exception e) {
            LOGGER.error("读取图片失败：" + e.getMessage());
        }
    }

    // page接收整理分页条件
    // 只要是实体类型（JavaBean）在方法传入中做参数，springMVC都会将其传入到Model里面

    @RequestMapping(path = "/detail/{postId}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable("postId") int postId, Model model, Page page) {
        // 查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        model.addAttribute("post", post);

        model.addAttribute("currentUser", hostHolder.getUser());

//        System.out.println(post.getHtmlcontent());

        // 更新浏览次数
        int formerViews = post.getViews();
        discussPostService.updateViews(postId, formerViews + 1);

        post.setViews(formerViews + 1);
        elasticSearchService.saveDiscussPost(post);

        // 查询作者
        User author = userService.findUserById(post.getUserId());
        model.addAttribute("author", author);

        // 点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_POST, postId);
        model.addAttribute("likeCount", likeCount);

        // 点赞状态，若未登陆则显示没点过赞
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_POST, postId);
        model.addAttribute("likeStatus", likeStatus);

        // 设置帖子评论的分页信息
        // 每页显示五条评论
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        // 总共有多少条评论，从帖子表查，这样比从评论表查效率要高
        page.setRows(post.getCommentCount());

        model.addAttribute("page", page);

        // 评论：给帖子的评论
        // 回复：给评论的评论

        // 存储整个帖子的评论
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_POST, postId,
                page.getOffSet(), page.getLimit());

        // 用于存储整个帖子的评论和其作者的信息
        List<Map<String, Object>> CommentVoList = new ArrayList<>();

        // 遍历整个commentList从而找到所有的回复
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 初始化
                HashMap<String, Object> commentVo = new HashMap<>();

                // 存储评论信息
                commentVo.put("comment", comment);

                // 存储用户信息
                User user = userService.findUserById(comment.getUserId());
                commentVo.put("user", user);

                // 点赞数
                likeCount = likeService.findEntityLikeCount(ENTITY_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // 点赞状态，若未登陆则显示没点过赞
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 得到回复的列表，不分页
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                // 用于存储回复的信息
                List<Map<String, Object>> replyVoList = new ArrayList<>();

                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // 初始化
                        HashMap<String, Object> replyVo = new HashMap<>();

                        // 存储回复信息
                        replyVo.put("reply", reply);

                        // 存储回复用户信息
                        User u = userService.findUserById(reply.getUserId());
                        replyVo.put("user", u);

                        // 点赞数
                        likeCount = likeService.findEntityLikeCount(ENTITY_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        // 点赞状态，若未登陆则显示没点过赞
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        // 是否是对评论的回复
                        User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", targetUser);

                        // 向VOList中添加数据
                        replyVoList.add(replyVo);
                    }
                }

                // 向VOList中添加数据
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                // 向VOList中添加数据
                CommentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", CommentVoList);

        return "/site/discuss-detail";
    }

    // 置顶

    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int postId/*, int type*/) {
        // 1表示置顶，0表示正常的帖子
        discussPostService.updateType(postId, 1);

        // 触发一次发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_POST)
                .setEntityId(postId)
                .setUserId(hostHolder.getUser().getId());

        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0);
    }

    // 加精

    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int postId) {
        // 1表示帖子加精，0表示正常的帖子
        discussPostService.updateStatus(postId, 1);

        // 触发一次发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_POST)
                .setEntityId(postId)
                .setUserId(hostHolder.getUser().getId());

        eventProducer.fireEvent(event);

        // 计算帖子分数
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, postId);

        return CommunityUtil.getJsonString(0);
    }

    // 拉黑

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int postId) {
        // 2表示帖子被拉黑
        discussPostService.updateStatus(postId, 2);

        // 触发一次删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setEntityType(ENTITY_POST)
                .setEntityId(postId)
                .setUserId(hostHolder.getUser().getId());

        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0);
    }

}
