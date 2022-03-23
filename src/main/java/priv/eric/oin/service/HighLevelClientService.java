package priv.eric.oin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.common.TimeUtil;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Eric EricTownsChina@outlook.com
 * create 2022/1/3 22:39
 * <p>
 * Desc:
 */
@Slf4j
@Component
public class HighLevelClientService {

    @Value("${spring.application.name}")
    private String prefix;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    private HighLevelClientService() {}

    public <T> List<T> getResultList(List<String> indices, SearchSourceBuilder sourceBuilder, Class<T> clazz) {
        // 装填查询信息
        SearchRequest searchRequest = new SearchRequest(indices.toArray(new String[]{}));
        searchRequest.source(sourceBuilder);

        SearchResponse response = null;
        try {
            // 发起查询
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("=====IO异常, 请稍后重试!");
        }

        List<T> resultList = new ArrayList<>();
        if (null == response) {
            log.error("=====获取查询结果失败!");
            return resultList;
        }

        // 解析查询结果
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String resultString = hit.getSourceAsString();
            T t = JSON.parseObject(resultString, clazz);
            resultList.add(t);


            // 高亮部分处理
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (0 == highlightFields.size()) {
                continue;
            }

            for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
                String fieldName = entry.getKey();
                HighlightField highField = entry.getValue();

                Text[] titleTexts = highField.fragments();
                StringBuilder highFieldSb = new StringBuilder();
                for (Text text : titleTexts) {
                    highFieldSb.append(text);
                }
                // 将追加了高亮标签的串值重新填充到对应的对象
                try {
                    Field field = t.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(t, highFieldSb.toString());
                } catch (NoSuchFieldException | IllegalAccessException ignore) {

                }
            }

        }

        // 返回数据
        return resultList;
    }

    public <T> List<T> getResultList(String index, SearchSourceBuilder sourceBuilder, Class<T> clazz) {
        return getResultList(Collections.singletonList(index), sourceBuilder, clazz);
    }


    //------------------------------------------------------- private
    private String generateIndexName() {
        // 验证
        if (StrUtil.isBlank(prefix)) {
            log.warn("未指定索引所属项目名称");
            return null;
        }

        // 当前时间
        long now = SystemClock.now();
        // 日期
        String today = DateUtil.date(now).toString("yyyy-MM-dd");

        // 添加索引前缀, 返回
        return prefix + "." + today;
    }

    public static void main(String[] args) {
        HighLevelClientService service = new HighLevelClientService();
        System.out.println(service.generateIndexName());
    }
}
