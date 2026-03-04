package com.springwork.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springwork.common.VerifyCodeGenerator;
import com.springwork.entity.VerifyCode;
import com.springwork.mapper.VerifyCodeMapper;
import com.springwork.utils.EmailTemplateUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailSendService {

    @Autowired
    private VerifyCodeMapper verifyCodeMapper;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 邮件主题
    private final String emailSubject = "【博客平台】您的验证码，请在有效期内使用";
    // 验证码有效期
    private final Integer expireMinutes = 10;

    // 发送验证码邮件（无CodeEmail版本）
    public void sendVerifyCodeEmail(String toEmail) {
        // 1. 生成6位验证码
        String verifyCode = VerifyCodeGenerator.generate6DigitCode();

        // 2. 删除旧验证码
        LambdaQueryWrapper<VerifyCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerifyCode::getEmail, toEmail)
                .eq(VerifyCode::getUsed, 0)
                .gt(VerifyCode::getExpireTime, LocalDateTime.now());
        verifyCodeMapper.delete(wrapper);

        // 3. 保存新验证码到数据库
        VerifyCode codeEntity = new VerifyCode();
        codeEntity.setEmail(toEmail);
        codeEntity.setCode(verifyCode);
        codeEntity.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));
        verifyCodeMapper.insert(codeEntity);

        // 4. 生成邮件正文
        String content = EmailTemplateUtil.generateVerifyCodeContent(verifyCode, expireMinutes);

        // 5. 发送邮件
        sendEmail(toEmail, emailSubject, content);
    }

   //校验验证码
    public boolean checkVerifyCode(String email, String inputCode) {
        // 1. 先查询该邮箱最新的未使用验证码（按过期时间倒序，避免旧验证码干扰）
        LambdaQueryWrapper<VerifyCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerifyCode::getEmail, email)
                .eq(VerifyCode::getUsed, 0) // 未使用
                .orderByDesc(VerifyCode::getExpireTime) // 优先查最新生成的
                .last("LIMIT 1"); // 只查最新的一条

        VerifyCode verifyCode = verifyCodeMapper.selectOne(wrapper);

        // 2. 验证码不存在 → 无效
        if (verifyCode == null) {
            return false;
        }

        // 3. 校验输入的验证码是否匹配（核心：先匹配验证码，再判断过期）
        if (!verifyCode.getCode().equals(inputCode)) {
            return false;
        }

        // 4. 修复过期判断：兼容LocalDateTime时间精度，允许1分钟误差
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = verifyCode.getExpireTime();
        // 正确逻辑：当前时间 <= 过期时间 → 有效（新增1分钟误差，避免秒级差导致误判）
        if (now.isAfter(expireTime.plusMinutes(1))) {
            return false;
        }

        // 5. 标记为已使用，防止重复使用
        verifyCode.setUsed(1);
        verifyCodeMapper.updateById(verifyCode);

        return true;
    }

    // 底层邮件发送
    private void sendEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail); // 发件人
            helper.setTo(toEmail);     // 收件人
            helper.setSubject(subject); // 主题
            helper.setText(content, false); // 正文
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }
}