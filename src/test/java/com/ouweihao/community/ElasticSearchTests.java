package com.ouweihao.community;

import com.ouweihao.community.dao.DiscussPostMapper;
import com.ouweihao.community.dao.elasticsearch.DiscussPostRepository;
import com.ouweihao.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTests {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 用于解决DiscussPOSTRepository无法解决的问题
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(107));
    }

    @Test
    public void testSaveAll() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100, 0, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(163, 0, 100, 0, 0));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setHtmlcontent("测试修改，新人灌水");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete() {
//        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchQuery() {
        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        // 查询底层调用了elasticTemplate.queryForPage(searchQuery, class, SearchResultMapper)
        // 查到的数据交由SearchResultMapper处理，进行高亮显示的时候，数据有两份，本该由SearchResultMapper处理，但他没处理
        // 高亮显示是处理了的，底层获得了高亮显示的值，但是没有返回

        Page<DiscussPost> posts = discussPostRepository.search(query);
        // 有多少条数据
        System.out.println(posts.getTotalElements());
        // 有多少页
        System.out.println(posts.getTotalPages());
        // 当前在第几页
        System.out.println(posts.getNumber());
        // 每页有多少条数据
        System.out.println(posts.getSize());
        for (DiscussPost post : posts) {
            System.out.println(post);
        }
    }

    @Test
    public void testByElasticsearchTemplate() {
        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("test", "title", "htmlcontent"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("htmlcontent").preTags("<em>").postTags("</em>")
                ).build();

        Page<DiscussPost> posts = elasticsearchTemplate.queryForPage(query, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                SearchHits hits = searchResponse.getHits();

                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                List<DiscussPost> postList = new ArrayList<>();

                for (SearchHit hit : hits) {

                    DiscussPost post = new DiscussPost();

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.parseInt(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.parseInt(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String mdcontent = hit.getSourceAsMap().get("mdcontent").toString();
                    post.setMdcontent(mdcontent);

                    String htmlcontent = hit.getSourceAsMap().get("htmlcontent").toString();
                    post.setHtmlcontent(htmlcontent);

                    String type = hit.getSourceAsMap().get("type").toString();
                    post.setType(Integer.parseInt(type));

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.parseInt(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.parseLong(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.parseInt(commentCount));

                    String score = hit.getSourceAsMap().get("score").toString();
                    post.setScore(Double.parseDouble(score));

                    // 处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("htmlcontent");
                    if (contentField != null) {
                        post.setHtmlcontent(contentField.getFragments()[0].toString());
                    }

                    postList.add(post);
                }

                return new AggregatedPageImpl(postList, pageable,
                        hits.getTotalHits(), searchResponse.getAggregations(), hits.getMaxScore());
            }
        });

        // 有多少条数据
        System.out.println(posts.getTotalElements());
        // 有多少页
        System.out.println(posts.getTotalPages());
        // 当前在第几页
        System.out.println(posts.getNumber());
        // 每页有多少条数据
        System.out.println(posts.getSize());
        for (DiscussPost post : posts) {
            System.out.println(post);
        }

    }

}
