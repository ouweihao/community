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

    @Value("${community.path.attachUpload}")
    private String uploadAttachDir;

    @RequestMapping(path = "/add", method = RequestMethod.GET)
    public String getAddPostPage(Model model) {

        DiscussPost post = new DiscussPost();

        model.addAttribute("post", post);

        // ?????????????????????
        List<Section> allSections = sectionService.getAllSections();
        model.addAttribute("sections", allSections);

        return "/site/discusspost_input";
    }


    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public String addDiscussPost(String title, @RequestParam(name = "my-editormd-markdown-doc") String mdContent,
                                 @RequestParam(name = "my-editormd-html-code") String[] htmlContent,
                                 @RequestParam(name = "attachFile") MultipartFile attachFile,
                                 HttpServletRequest request, int sectionId, RedirectAttributes attributes) {

        User currentUser = hostHolder.getUser();

        // ?????????????????????????????????
        if (currentUser == null) {
            return CommunityUtil.getJsonString(403, "????????????????????????");
        }

//        System.out.println(htmlContent[1]);
//        System.out.println(mdContent);

        String commentable = request.getParameter("commentable");
        System.out.println("commentable: " + commentable);

        DiscussPost post = new DiscussPost();

        post.setUserId(currentUser.getId());
        post.setTitle(title);
        post.setMdcontent(mdContent);
        post.setHtmlcontent(htmlContent[0]);
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        post.setCommentable(Integer.valueOf(commentable));
        post.setSectionId(sectionId);
        post.setViews(0);

        if (attachFile.isEmpty()) {
            post.setAttachName(null);
            post.setAttachUrl(null);
        } else {
            Map<String, String> res = uploadAttach(attachFile);
            post.setAttachName(res.get("filename"));
            post.setAttachUrl(res.get("attachUrl"));
        }

        discussPostService.addDiscussPost(post);
        elasticSearchService.saveDiscussPost(post);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_POST)
                .setEntityId(post.getId())
                .setAuthorId(currentUser.getId());

        eventProducer.fireEvent(event);

        // ??????????????????
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, post.getId());

        attributes.addFlashAttribute("publishstatus", 1);
        attributes.addFlashAttribute("publishmsg", "?????????????????????");

        // ???????????????????????????
        return "redirect:/index";

//        return CommunityUtil.getJsonString(0, "???????????????");
    }

    @RequestMapping(path = "/updatepost/{id}", method = RequestMethod.POST)
    public String updatePost(@PathVariable(name = "id") int id, String title,
                             @RequestParam(name = "my-editormd-markdown-doc") String mdContent,
                             @RequestParam(name = "my-editormd-html-code") String[] htmlContent,
                             @RequestParam(name = "attachFile") MultipartFile attachFile,
                             HttpServletRequest request, int sectionId, RedirectAttributes attributes) {


        System.out.println("attachFile = " + attachFile);
        System.out.println("attachFile.isEmpty() = " + attachFile.isEmpty());

        User currentUser = hostHolder.getUser();

        // ?????????????????????????????????
        if (currentUser == null) {
            return CommunityUtil.getJsonString(403, "????????????????????????");
        }

        DiscussPost newPost = new DiscussPost();
        DiscussPost post = discussPostService.findDiscussPostById(id);

        // ??????????????????
        elasticSearchService.deleteDiscussPost(id);

        String commentable = request.getParameter("commentable");

        // ????????????

        newPost.setId(post.getId());
        newPost.setUserId(currentUser.getId());
        newPost.setTitle(title);
        newPost.setMdcontent(mdContent);
        newPost.setHtmlcontent(htmlContent[0]);
        newPost.setType(post.getType());
        newPost.setStatus(post.getStatus());
        newPost.setCreateTime(post.getCreateTime());
        newPost.setUpdateTime(new Date());
        newPost.setCommentable(Integer.valueOf(commentable));
        newPost.setCommentCount(post.getCommentCount());
        newPost.setSectionId(sectionId);
        newPost.setViews(post.getViews());
        newPost.setScore(post.getScore());

        // ?????????????????????????????????????????????
        if (attachFile.isEmpty()) {
            newPost.setAttachName(post.getAttachName());
            newPost.setAttachUrl(post.getAttachUrl());
        } else {
            Map<String, String> res = uploadAttach(attachFile);
            newPost.setAttachName(res.get("filename"));
            newPost.setAttachUrl(res.get("attachUrl"));
        }

        // ??????
        discussPostService.updatePost(newPost);
        // ??????????????????
        elasticSearchService.saveDiscussPost(newPost);

        // ??????????????????
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, post.getId());

        attributes.addFlashAttribute("publishstatus", 1);
        attributes.addFlashAttribute("publishmsg", "?????????????????????");

        // ???????????????????????????
        return "redirect:/index";

    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject editormdPic(@RequestParam(value = "editormd-image-file", required = true) MultipartFile file) throws Exception {

        // ?????????????????????
        String fileName = file.getOriginalFilename();

        String suffix = fileName.substring(fileName.lastIndexOf("."));

        fileName = CommunityUtil.generateUUID() + suffix;

        // ?????????????????????
        fileName = CommunityUtil.generateUUID() + suffix;
        // ???????????????????????????
        File dest = new File(uploadPath + "/" + fileName);

//        File targetFile = new File(uploadPath, fileName);
//        if(!targetFile.exists()){
//            targetFile.mkdirs();
//        }

        System.out.println(uploadPath);

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("?????????????????????" + e.getMessage());
            throw new RuntimeException("?????????????????????????????????????????????", e);
        }

        System.out.println(domain);

        // ????????????  http://localhost:8080/community/discuss/EditorImage/xxx.png
        // ????????????  http://localhost:8080/community/img/upload/xxx.png
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
        // ??????????????????????????????
        fileName = uploadPath + "/" + fileName;

        // ??????????????????
        String suffix = fileName.substring(fileName.lastIndexOf('.'));

        // ????????????
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
            LOGGER.error("?????????????????????" + e.getMessage());
        }
    }

    private Map<String, String> uploadAttach(MultipartFile attachFile) {

        Map<String, String> res = new HashMap<>();

//        System.out.println("attachFile = " + attachFile);

        if (attachFile == null) {
            return res;
        }

        // ????????????????????????
        String filename = attachFile.getOriginalFilename();

//        System.out.println("filename = " + filename);

        // ????????????????????????
        String suffix = null;
        if (filename.lastIndexOf('.') != -1) {
            suffix = filename.substring(filename.lastIndexOf('.'));
        }

//        if (suffix == null) {
//            return res;
//        }

        // ???????????????????????????????????????
        res.put("filename", filename);

        // ?????????????????????
        filename = CommunityUtil.generateUUID() + suffix;

        String attachUrl = domain + contextPath + "/discuss/attach/" + filename;

        res.put("attachUrl", attachUrl);

//        for (Map.Entry<String, String> entry : res.entrySet()) {
//            System.out.println(entry.getKey() + " : " + res.get(entry.getKey()));
//        }

        // ???????????????????????????
        File dest = new File(uploadAttachDir + "/" + filename);

        try {
            attachFile.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("?????????????????????" + e.getMessage());
            throw new RuntimeException("?????????????????????????????????????????????", e);
        }
//
//        return CommunityUtil.getJsonString(0, "??????????????????");

        return res;

    }

    @RequestMapping(path = "/attach/{fileName}", method = RequestMethod.GET)
    public void downloadAttach(@PathVariable(name = "fileName") String fileName, HttpServletResponse response) {

        response.setContentType("application/octet-stream");

        response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

        // ??????IE???????????????
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "No-cache");
        response.setDateHeader("Expires", 0);

        // ??????????????????????????????
        fileName = uploadAttachDir + "/" + fileName;
        System.out.println("fileName = " + fileName);

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
            LOGGER.error("?????????????????????" + e.getMessage());
        }

    }

    @RequestMapping(path = "/deleteAttach/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String deleteAttach(@PathVariable(name = "id") int postId) {
        discussPostService.updateAttach(postId, null, null);
        return CommunityUtil.getJsonString(0, "???????????????????????????");
    }

    // page????????????????????????
    // ????????????????????????JavaBean?????????????????????????????????springMVC?????????????????????Model??????

    @RequestMapping(path = "/detail/{postId}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable("postId") int postId, Model model, Page page) {
        // ????????????
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        model.addAttribute("post", post);

        model.addAttribute("currentUser", hostHolder.getUser());

//        System.out.println(post.getHtmlcontent());

        // ??????????????????
        int formerViews = post.getViews();
        discussPostService.updateViews(postId, formerViews + 1);

        post.setViews(formerViews + 1);
        elasticSearchService.saveDiscussPost(post);

        // ????????????
        User author = userService.findUserById(post.getUserId());
        model.addAttribute("author", author);

        // ?????????
        long likeCount = likeService.findEntityLikeCount(ENTITY_POST, postId);
        model.addAttribute("likeCount", likeCount);

        // ????????????????????????????????????????????????
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_POST, postId);
        model.addAttribute("likeStatus", likeStatus);

        // ?????????????????????????????????
        // ????????????????????????
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        // ?????????????????????????????????????????????????????????????????????????????????
        page.setRows(post.getCommentCount());

        model.addAttribute("page", page);

        // ???????????????????????????
        // ???????????????????????????

        // ???????????????????????????
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_POST, postId,
                page.getOffSet(), page.getLimit());

        // ??????????????????????????????????????????????????????
        List<Map<String, Object>> CommentVoList = new ArrayList<>();

        // ????????????commentList???????????????????????????
        if (commentList != null) {
            for (Comment comment : commentList) {
                // ?????????
                HashMap<String, Object> commentVo = new HashMap<>();

                // ??????????????????
                commentVo.put("comment", comment);

                // ??????????????????
                User user = userService.findUserById(comment.getUserId());
                commentVo.put("user", user);

                // ?????????
                likeCount = likeService.findEntityLikeCount(ENTITY_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // ????????????????????????????????????????????????
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // ?????????????????????????????????
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                // ???????????????????????????
                List<Map<String, Object>> replyVoList = new ArrayList<>();

                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // ?????????
                        HashMap<String, Object> replyVo = new HashMap<>();

                        // ??????????????????
                        replyVo.put("reply", reply);

                        // ????????????????????????
                        User u = userService.findUserById(reply.getUserId());
                        replyVo.put("user", u);

                        // ?????????
                        likeCount = likeService.findEntityLikeCount(ENTITY_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        // ????????????????????????????????????????????????
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        // ???????????????????????????
                        User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", targetUser);

                        // ???VOList???????????????
                        replyVoList.add(replyVo);
                    }
                }

                // ???VOList???????????????
                commentVo.put("replys", replyVoList);

                // ????????????
                int replyCount = commentService.findCommentCount(ENTITY_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                // ???VOList???????????????
                CommentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", CommentVoList);

        return "/site/discuss-detail";
    }

    // ??????

    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int postId/*, int type*/) {
        // 1???????????????0?????????????????????
        discussPostService.updateType(postId, 1);

        // ????????????????????????
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_POST)
                .setEntityId(postId)
                .setUserId(hostHolder.getUser().getId());

        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0);
    }

    // ????????????

    @RequestMapping(path = "/untop", method = RequestMethod.POST)
    @ResponseBody
    public String setUnTop(int postId/*, int type*/) {
        // 1???????????????0?????????????????????
        discussPostService.updateType(postId, 0);

        elasticSearchService.deleteDiscussPost(postId);

        DiscussPost post = discussPostService.findDiscussPostById(postId);
        post.setType(0);

        elasticSearchService.saveDiscussPost(post);

        return CommunityUtil.getJsonString(0);
    }

    // ??????

    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int postId) {
        // 1?????????????????????0?????????????????????
        discussPostService.updateStatus(postId, 1);

        // ????????????????????????
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_POST)
                .setEntityId(postId)
                .setUserId(hostHolder.getUser().getId());

        eventProducer.fireEvent(event);

        // ??????????????????
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, postId);

        return CommunityUtil.getJsonString(0);
    }

    // ????????????

    @RequestMapping(path = "/unwonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setUnWonderful(int postId) {
        // 1?????????????????????0?????????????????????
        discussPostService.updateStatus(postId, 0);

        // ????????????????????????
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityType(ENTITY_POST)
                .setEntityId(postId)
                .setUserId(hostHolder.getUser().getId());

        eventProducer.fireEvent(event);

        // ??????????????????
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, postId);

        return CommunityUtil.getJsonString(0);
    }

    // ??????

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int postId) {
        // 2?????????????????????
        discussPostService.updateStatus(postId, 2);

        // ????????????????????????
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setEntityType(ENTITY_POST)
                .setEntityId(postId)
                .setUserId(hostHolder.getUser().getId());

        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0);
    }

}
