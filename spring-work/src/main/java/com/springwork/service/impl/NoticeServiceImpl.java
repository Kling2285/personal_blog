package com.springwork.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springwork.entity.Notice;
import com.springwork.mapper.NoticeMapper;
import com.springwork.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
}
