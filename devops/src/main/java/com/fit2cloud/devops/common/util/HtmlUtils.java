package com.fit2cloud.devops.common.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HtmlUtils {

    public static  <T> List<T> selectElementFromBody(String content, String selector, Function<Element, T> convertFun) {
        List<T> resultList = new ArrayList<>();
        if (StringUtils.isNotBlank(content)) {
            Elements elements = Jsoup.parse(content).body().select(selector);
            elements.forEach(element -> resultList.add(convertFun.apply(element)));
        }
        return resultList;
    }

}
