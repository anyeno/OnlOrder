package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImlp implements UserService {
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        // 通过httpclient构造登录凭证校验请求
        // 构造请求参数
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("grant_type", "authorization_code");
        params.put("js_code", userLoginDTO.getCode());
        String res = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", params);
        // 解析相应请求 get openid
        JSONObject jsonObject = JSON.parseObject(res);
        String openid = (String) jsonObject.get("openid");
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 新用户则注册
        User user = userMapper.selectByOpenid(openid);
        if(user == null){
            user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            user.setName(openid.substring(0,5));
            userMapper.insert(user);
        }
        // 不是新用户直接返回user
        return user;
    }
}
