package com.flashsalex.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flashsalex.common.Result;
import com.flashsalex.dto.request.GoodsCreateRequest;
import com.flashsalex.dto.request.GoodsUpdateRequest;
import com.flashsalex.entity.Goods;
import com.flashsalex.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-商品管理", description = "商品增删改查")
@RestController
@RequestMapping("/api/admin/goods")
@RequiredArgsConstructor
public class AdminGoodsController {

    private final GoodsService goodsService;

    @Operation(summary = "创建商品")
    @PostMapping
    public Result<Long> createGoods(@RequestBody @Valid GoodsCreateRequest request) {
        Long id = goodsService.createGoods(request);
        return Result.success(id);
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<Void> updateGoods(@PathVariable Long id, @RequestBody GoodsUpdateRequest request) {
        goodsService.updateGoods(id, request);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        goodsService.deleteGoods(id);
        return Result.success();
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<Goods> getGoods(@PathVariable Long id) {
        return Result.success(goodsService.getGoods(id));
    }

    @Operation(summary = "商品列表")
    @GetMapping
    public Result<Page<Goods>> listGoods(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(goodsService.listGoods(page, size));
    }
}
