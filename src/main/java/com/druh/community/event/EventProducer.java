package com.druh.community.event;

import com.alibaba.fastjson.JSONObject;
import com.druh.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理时间
     * @param event 事件对象
     */
    public void fireEvent(Event event) {
        // 将事件发布到指定主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
