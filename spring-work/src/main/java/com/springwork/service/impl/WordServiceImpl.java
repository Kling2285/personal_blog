package com.springwork.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springwork.entity.Word;
import com.springwork.mapper.WordMapper;
import com.springwork.service.WordService;
import org.springframework.stereotype.Service;

@Service
public class WordServiceImpl extends ServiceImpl<WordMapper, Word> implements WordService {

}