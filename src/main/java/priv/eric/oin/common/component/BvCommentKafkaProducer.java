package priv.eric.oin.common.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;

/**
 * @author EricTownsChina@outlook.com
 * create 2022/2/13 23:44
 * <p>
 * Desc:
 */
@Slf4j
@Component
public class BvCommentKafkaProducer {

    private static final String TOPIC_NAME = "bvComment";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BvCommentKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 生产消息到kafka, topic为bvComment
     *
     * @param obj      消息
     */
    public void send(Object obj) {
        // 生产
        kafkaTemplate.send(TOPIC_NAME, obj);
        log.info("file {} send", obj);
    }

}
