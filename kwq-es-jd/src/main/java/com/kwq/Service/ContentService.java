package com.kwq.Service;

import com.alibaba.fastjson.JSON;
import com.kwq.utils.HtmlParseUtil;
import com.pojo.Content;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//业务编写
@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //1.解析数据放入es索引中
    public boolean parseContent(String keywords) throws IOException {
        List<Content> contents = new HtmlParseUtil().parseJD(keywords);
        //把查询到的数据放入es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2ms");
        for (int i=0;i<contents.size();i++){
            bulkRequest.add(new IndexRequest("jd_goods")
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    //2、获取这些数据实现搜索功能
    public List<Map<String,Object>> searchPage(String keyword,int pageNo,int pageSize) throws IOException {
        if (pageNo<=1){
            pageNo=1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精准匹配
       /* TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);*/
        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("title", keyword);
        sourceBuilder.query(fuzzyQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }
    return  list;}  //2、获取这些数据实现搜索功能

    //3、获取这些数据实现搜索高亮功能
    public List<Map<String,Object>> searchPageHighlightBuilder(String keyword,int pageNo,int pageSize) throws IOException {
        if (pageNo<=1){
            pageNo=1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精准匹配
       /* TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);*/
        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("title", keyword);
        sourceBuilder.query(fuzzyQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false); //关闭多个高亮显示！只显示第一个
        highlightBuilder.preTags("<span style='color:red'>"); //高亮字段的前缀
        highlightBuilder.postTags("</span>"); //高亮字段的后缀
        sourceBuilder.highlighter(highlightBuilder);


        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            //解析高亮的字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            //把所有的标题获取出来
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();//原来的结果
            //解析高亮的字段，将原来的字段换为我们高亮的字段即可！
            if (title!=null){
                Text[] fragments = title.fragments();
                String n_title="";
                for (Text text : fragments) {
                    n_title+=text;
                    sourceAsMap.put("title",n_title);  //高亮字段替换掉原来的内容即可！
                }
            } list.add(sourceAsMap);

        }
    return  list;}

}
