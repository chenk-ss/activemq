package org.apache.activemq.broker.region.chenk;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;

/**
 * @Author chenk
 * @create 2021/1/15 9:42
 */
public class AuthPlugin implements BrokerPlugin {
    public AuthPlugin() {
    }

    @Override
    public Broker installPlugin(Broker broker) throws Exception {
        return new AuthBroker(broker);
    }
}