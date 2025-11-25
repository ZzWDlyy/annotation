package com.slowcoder.annotation.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.slowcoder.annotation.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 发送邮箱验证码
     * @param email 目标邮箱
     */
    @Override
    public void sendEmailCaptcha(String email) {
        // 1. 生成 6 位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 2. 存入 Redis，有效期 5 分钟
        String key = "captcha:email:" + email;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        // 3. 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@qq.com");  // 发件人（与配置文件一致）
        message.setTo(email);  // 收件人
        message.setSubject("【标注平台】验证码");
        message.setText("您的验证码是：" + code + "，5分钟内有效，请勿泄露。");

        mailSender.send(message);
    }

    /**
     * 校验验证码
     * @param email 邮箱
     * @param code 用户输入的验证码
     * @return 是否正确
     */
    @Override
    public boolean verifyCaptcha(String email, String code) {
        String key = "captcha:email:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            return false; // 验证码已过期
        }

        if (storedCode.equals(code)) {
            // 验证成功后删除验证码（防止重复使用）
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }
}
