package org.apache.activemq.broker.region.chenk;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.security.AbstractAuthenticationBroker;
import org.apache.activemq.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author chenk
 * @create 2021/1/15 9:51
 */
public class AuthBroker extends AbstractAuthenticationBroker {

    private static Logger LOG = LoggerFactory.getLogger(AuthBroker.class);

    public AuthBroker(Broker next) {
        super(next);
    }

    /**
     * <p>
     * 创建连接的时候拦截
     * </p>
     */
    @Override
    public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
        LOG.debug("addConnection");
        SecurityContext securityContext = context.getSecurityContext();
        if (securityContext == null) {
            securityContext = authenticate(info.getUserName(), info.getPassword(), null);
            context.setSecurityContext(securityContext);
            securityContexts.add(securityContext);
        }

        try {
            super.addConnection(context, info);
        } catch (Exception e) {
            securityContexts.remove(securityContext);
            context.setSecurityContext(null);
            throw e;
        }
    }

    /**
     * 得到用户信息
     * <p>Title: getUser</p>
     *
     * @param username
     * @return
     */
    private String getUser(String username) {
        Object object = RedisPlugin.getByKey("TokenOf" + username);
        return object == null ? null : (String) object;
    }

    /**
     * 认证
     * <p>Title: authenticate</p>
     */
    @Override
    public SecurityContext authenticate(String username, String password, X509Certificate[] peerCertificates) throws SecurityException {
        SecurityContext securityContext = null;
        String token = getUser(username);
        //验证用户信息
        LOG.info("user : {} , pwd : {}", username, token);
        if (token != null && token.equals(password)) {
            securityContext = new SecurityContext(username) {
                @Override
                public Set<Principal> getPrincipals() {
                    Set<Principal> groups = new HashSet<Principal>();
                    groups.add(new GroupPrincipal("users"));//默认加入了users的组
                    return groups;
                }
            };
        } else {
            LOG.error("验证失败");
            throw new SecurityException("验证失败");
        }
        return securityContext;
    }

}
