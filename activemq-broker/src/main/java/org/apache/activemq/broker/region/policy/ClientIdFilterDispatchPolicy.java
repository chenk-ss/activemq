package org.apache.activemq.broker.region.policy;

import org.apache.activemq.broker.region.MessageReference;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.filter.MessageEvaluationContext;
import org.apache.activemq.util.ByteSequence;
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

    public static final String CLIENTID = "_CLIENTID";

    public boolean dispatch(MessageReference node, MessageEvaluationContext msgContext, List<Subscription> consumers) throws Exception {
        //指定特定后缀名的topic进入自定义分发策略
        String _clientId = null;
        LOG.info(System.currentTimeMillis() + "进入ClientIdFilterDispatchPolicy");
        LOG.info("node:" + node.toString());

        ByteSequence byteSequence = node.getMessage().getContent();
        byte[] bytes = byteSequence.data;
        for (int i = 0; i < bytes.length; i++) {
            // 字符包含"_"时，判断是不是_CLIENTID字段
            if (bytes[i] == 95 && i + CLIENTID.length() < bytes.length) {
                boolean boo = true;
                for (int j = 0; j < CLIENTID.length(); j++) {
                    if (!(bytes[i + j] == CLIENTID.substring(j, j + 1).getBytes()[0])) {
                        boo = false;
                        break;
                    }
                }
                if (boo) {
                    int clientIdLen = 0;
                    // TODO 需要加上是不是最外层的判断，避免message中也有CLIENTID字段
                    while (true) {
                        int byteNum = bytes[i + CLIENTID.length() + 1 + clientIdLen];
                        // 125和44分别代表"}"和",",当下一个字符为125或44时，获取_CLIENTID结束
                        if (byteNum != 125 && byteNum != 44) {
                            clientIdLen++;
                        } else {
                            break;
                        }
                    }
                    byte[] clientByte = new byte[clientIdLen];
                    // 截取_CLIENTID信息
                    for (int j = 0; j < clientIdLen; j++) {
                        clientByte[j] = bytes[i + CLIENTID.length() + j + 1];
                    }
                    _clientId = new String(clientByte);
                    LOG.info("_clientId:" + _clientId);
                    break;
                }
            }
        }

        LOG.info("-----结束循环----");

        if (_clientId == null || "".equals(_clientId) || "null".equals(_clientId)) {
            return super.dispatch(node, msgContext, consumers);
        }

        ActiveMQDestination _destination = node.getMessage().getDestination();

        int count = 0;
        for (Subscription sub : consumers) {
            LOG.info("isTopic:" + _destination.isTopic());
            LOG.info("getClientId:" + sub.getContext().getClientId());
            // Don't deliver to browsers
//            LOG.info("sub.getConsumerInfo().isBrowser():" + sub.getConsumerInfo().isBrowser());
            if (sub.getConsumerInfo().isBrowser()) {
                continue;
            }
            // Only dispatch to interested subscriptions
            if (!sub.matches(node, msgContext)) {
                sub.unmatched(node);
                continue;
            }
            if (_clientId != null && _destination.isTopic() && _clientId.equals(sub.getContext().getClientId())) {
                LOG.info(sub.getContext().getClientId() + "匹配成功");
                //把消息推送给满足要求的subscription
                sub.add(node);
                count++;
            } else {
                sub.unmatched(node);
            }
        }
        if (count == 0) {
            LOG.error("没有找到用户名为:{}的连接", _clientId);
        }
        return count > 0;
    }
}