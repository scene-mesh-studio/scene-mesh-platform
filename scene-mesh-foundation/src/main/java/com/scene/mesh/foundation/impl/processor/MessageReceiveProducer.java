package com.scene.mesh.foundation.impl.processor;

import com.scene.mesh.foundation.spec.collector.ICollector;
import com.scene.mesh.foundation.spec.message.IMessageConsumer;
import com.scene.mesh.foundation.spec.message.MessageTopic;
import com.scene.mesh.foundation.spec.processor.IProcessInput;
import com.scene.mesh.foundation.spec.processor.IProcessOutput;
import com.scene.mesh.foundation.impl.helper.SimpleObjectHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageReceiveProducer<T> extends BaseProcessor {

    @Setter
    @Getter
    private IMessageConsumer messageConsumer;
    private MessageTopic messageTopic;
    @Getter
    private String topicName;
    @Setter
    @Getter
    private int batchSize;
    @Getter
    @Setter
    private Class<T> messageClass;

    public void setTopicName(String topicName) {
        this.topicName = topicName;
        this.messageTopic = new MessageTopic(topicName);
    }

    @Override
    protected void produce(IProcessInput input, IProcessOutput output) throws Exception {
        if (this.messageClass == null) {
            throw new NullPointerException("messageClass is null");
        }
        List<T> list = this.messageConsumer.receive(this.messageTopic, this.messageClass);
        ICollector collector = output.getCollector();
        if (list != null && !list.isEmpty()) {
            List<T> objs = this.handleMessageList(list);
            for (Object obj : objs) {
                collector.collect(obj);
                log.info("接收到消息:{}", SimpleObjectHelper.objectData2json(obj));
            }
        } else {

        }
    }

    protected List<T> handleMessageList(List<T> list) {
        return list;
    }

}
