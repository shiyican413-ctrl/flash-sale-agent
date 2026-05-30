package com.flashsalex.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flashsalex.common.BusinessException;
import com.flashsalex.common.ErrorCode;
import com.flashsalex.dto.request.GoodsCreateRequest;
import com.flashsalex.dto.request.GoodsUpdateRequest;
import com.flashsalex.entity.Goods;
import com.flashsalex.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsMapper goodsMapper;

    /**
     * 创建商品
     */
    public Long createGoods(GoodsCreateRequest request) {
        Goods goods = new Goods();
        goods.setGoodsName(request.getGoodsName());
        goods.setGoodsDesc(request.getGoodsDesc());
        goods.setOriginalPrice(request.getOriginalPrice());
        goods.setCoverUrl(request.getCoverUrl());
        goods.setStatus(1);
        goodsMapper.insert(goods);
        log.info("商品创建成功: id={}, name={}", goods.getId(), goods.getGoodsName());
        return goods.getId();
    }

    /**
     * 更新商品
     */
    public void updateGoods(Long id, GoodsUpdateRequest request) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品不存在");
        }
        if (request.getGoodsName() != null) goods.setGoodsName(request.getGoodsName());
        if (request.getGoodsDesc() != null) goods.setGoodsDesc(request.getGoodsDesc());
        if (request.getOriginalPrice() != null) goods.setOriginalPrice(request.getOriginalPrice());
        if (request.getCoverUrl() != null) goods.setCoverUrl(request.getCoverUrl());
        if (request.getStatus() != null) goods.setStatus(request.getStatus());
        goodsMapper.updateById(goods);
        log.info("商品更新成功: id={}", id);
    }

    /**
     * 删除商品
     */
    public void deleteGoods(Long id) {
        goodsMapper.deleteById(id);
        log.info("商品删除成功: id={}", id);
    }

    /**
     * 商品详情
     */
    public Goods getGoods(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品不存在");
        }
        return goods;
    }

    /**
     * 商品分页列表
     */
    public Page<Goods> listGoods(int page, int size) {
        return goodsMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, 1).orderByDesc(Goods::getId));
    }
}
