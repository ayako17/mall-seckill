package com.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.SeckillProductMapper;
import com.seckill.service.SeckillProductService;
import com.seckill.vo.SeckillProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillProductServiceImpl extends ServiceImpl<SeckillProductMapper, SeckillProduct> implements SeckillProductService {

    @Autowired
    private SeckillProductMapper seckillProductMapper;

    @Override
    public List<SeckillProductVO> getActiveSeckillProducts() {
        return seckillProductMapper.getActiveSeckillProducts();
    }
}