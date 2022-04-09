package com.kwq.utils;

import com.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
@Component
public class HtmlParseUtil {
    //测试下面的方法是否可用
    public static void main(String[] args) throws IOException {
        new HtmlParseUtil().parseJD("java").forEach(System.out::println);
    }
    public List<Content> parseJD(String keyword) throws IOException {
        //前提 需要联网
        System.out.println(keyword);
        String url = "https://search.jd.com/Search?keyword="+keyword;
        //解析网页（Jsoup返回Document就是浏览器的Document对象）
        Document document = Jsoup.parse(new URL(url), 130000);
        //所有你在js中可以使用的方法，这里都能用
        Element element = document.getElementById("J_goodsList");
        //System.out.println(element);
        //获取所有的li标签
        Elements elements = element.getElementsByTag("li");
        ArrayList<Content> goodsList = new ArrayList<>();
        //获取li元素中的内容  这里的e1就是li
        for (Element e1 : elements) {
            String img = e1.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = e1.getElementsByClass("p-price").eq(0).text();
            String title = e1.getElementsByClass("p-name").eq(0).text();
            Content content = new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);
            goodsList.add(content);
        }
        return goodsList;
    }
}
