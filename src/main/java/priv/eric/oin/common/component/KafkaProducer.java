package priv.eric.oin.common.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author EricTownsChina@outlook.com
 * create 2022/2/13 23:44
 * <p>
 * Desc: kafka推送工具方法
 */
@Slf4j
@Component
public class KafkaProducer {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 生产消息到kafka
     *
     * @param key 消息key
     * @param data 消息内容
     */
    public void send(String topicName, String key, Object data) {
//        log.info("===== 推送kafka, topic = {}, key = {}", topicName, key);
        kafkaTemplate.send(topicName, key, data.toString());
    }

}
