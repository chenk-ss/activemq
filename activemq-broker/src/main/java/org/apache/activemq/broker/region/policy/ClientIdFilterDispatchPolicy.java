package org.apache.activemq.broker.region.policy;

import org.apache.activemq.broker.region.MessageReference;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.filter.MessageEvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ClientIdFilter dispatch policy that sends a message to every subscription that
 * matches the message in consumer ClientId.
 *
 * @org.apache.xbean.XBean
 */

public class ClientIdFilterDispatchPolicy extends SimpleDispatchPolicy {

    private static Logger LOG = LoggerFactory.getLogger(ClientIdFilterDispatchPolicy.class);

    //可自定义消息目标id在消息属性中的key
    private String ptpClientId = "_CLIENTID";

    public boolean dispatch(MessageReference node, MessageEvaluationContext msgContext, List<Subscription> consumers) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("===============Enter ClientIdFilterDispatchPolicy........");
        }

        // 获取消息中的目标 客户端id
        Object clientId = node.getMessage().getProperty(ptpClientId);

        // 如果没有，直接广播
        if (clientId == null) {
            return super.dispatch(node, msgContext, consumers);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("===============Client id : " + clientId);
        }

        // 获取当前消息类型，此处主要是限制为主题模式
        ActiveMQDestination destination = node.getMessage().getDestination();
        int count = 0;
        // 遍历所有订阅者
        for (Subscription sub : consumers) {

            // 不交于浏览器
            if (sub.getConsumerInfo().isBrowser()) {
                continue;
            }

            // 只发送给感兴趣的订阅
            if (!sub.matches(node, msgContext)) {
                sub.unmatched(node);
                continue;
            }

            // 消息中带有的目标id不为空，也为主题模式，并且当前的消费者的id和消息中的目标id相同，则投递消息
            if (clientId != null && destination.isTopic() && clientId.equals(sub.getContext().getClientId())) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("==============Send p2p message to : " + clientId);
                    LOG.info("==============Topic : " + destination.isTopic());
                }
                sub.add(node);
                count++;
                return true;
            } else {
                sub.unmatched(node);
            }
        }
        return count > 0;
    }
}