package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import tk.mybatis.mapper.entity.Example;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends BaseServiceImpl<TbUser> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue itcastSmsQueue;

    @Value("${signName}")
    private String signName;

    @Value("${templateCode}")
    private String templateCode;

    @Override
    public PageInfo<TbUser> search(Integer pageNum, Integer pageSize, TbUser user) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbUser.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(user.getProperty())) {
            criteria.andLike("property", "%" + user.getProperty() + "%");
        }*/

        List<TbUser> list = userMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void sendSmsCode(String phone) {
        //生成随机的6位数值的字符串，
        String code = (long)((Math.random()*9+1)*100000) + "";
        System.out.println("---------------验证码---------------：" + code);
        //将用户的手机号和验证码存入redis中并设置5分钟过期
        redisTemplate.boundValueOps(phone).set(code);
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);

        //将4个消息发送到Mq队列中 item_sms_queue
        jmsTemplate.send(itcastSmsQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                //手机号
                mapMessage.setString("mobile",phone);
                //签名
                mapMessage.setString("signName",signName);
                //编号模板
                mapMessage.setString("templateCode",templateCode);
                //模板参数{"code":123456}
                mapMessage.setString("templateParam","{\"code\":"+code+"}");
                return mapMessage;
            }
        });
    }

    @Override
    public boolean checkSmsCode(String phone, String smsCode) {
        String code = (String) redisTemplate.boundValueOps(phone).get();
        if (smsCode.equals(code)){
            //返回之前将redis中的验证码删除
            redisTemplate.delete(phone);
            return true;
        }
        return false;
    }

}
