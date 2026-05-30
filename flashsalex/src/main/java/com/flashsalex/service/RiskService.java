package com.flashsalex.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashsalex.entity.RiskBlacklist;
import com.flashsalex.mapper.RiskBlacklistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 风控服务 - 黑名单管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskBlacklistMapper blacklistMapper;

    /**
     * 检查用户是否在黑名单中
     */
    public boolean isUserBlacklisted(Long userId) {
        return isBlacklisted("USER", String.valueOf(userId));
    }

    /**
     * 检查 IP 是否在黑名单中
     */
    public boolean isIpBlacklisted(String ip) {
        return isBlacklisted("IP", ip);
    }

    /**
     * 通用黑名单检查
     */
    private boolean isBlacklisted(String targetType, String targetValue) {
        RiskBlacklist record = blacklistMapper.selectOne(
                new LambdaQueryWrapper<RiskBlacklist>()
                        .eq(RiskBlacklist::getTargetType, targetType)
                        .eq(RiskBlacklist::getTargetValue, targetValue)
                        .eq(RiskBlacklist::getStatus, 1));

        if (record == null) {
            return false;
        }
        // 检查是否过期
        if (record.getExpireTime() != null && record.getExpireTime().isBefore(LocalDateTime.now())) {
            record.setStatus(0);
            blacklistMapper.updateById(record);
            return false;
        }
        return true;
    }

    /**
     * 添加到黑名单
     */
    public void addToBlacklist(String targetType, String targetValue, String reason, LocalDateTime expireTime) {
        RiskBlacklist existing = blacklistMapper.selectOne(
                new LambdaQueryWrapper<RiskBlacklist>()
                        .eq(RiskBlacklist::getTargetType, targetType)
                        .eq(RiskBlacklist::getTargetValue, targetValue));

        if (existing != null) {
            existing.setReason(reason);
            existing.setExpireTime(expireTime);
            existing.setStatus(1);
            blacklistMapper.updateById(existing);
        } else {
            RiskBlacklist record = new RiskBlacklist();
            record.setTargetType(targetType);
            record.setTargetValue(targetValue);
            record.setReason(reason);
            record.setExpireTime(expireTime);
            record.setStatus(1);
            blacklistMapper.insert(record);
        }
        log.info("黑名单添加: type={}, value={}, reason={}", targetType, targetValue, reason);
    }

    /**
     * 从黑名单移除
     */
    public void removeFromBlacklist(String targetType, String targetValue) {
        RiskBlacklist record = blacklistMapper.selectOne(
                new LambdaQueryWrapper<RiskBlacklist>()
                        .eq(RiskBlacklist::getTargetType, targetType)
                        .eq(RiskBlacklist::getTargetValue, targetValue));
        if (record != null) {
            record.setStatus(0);
            blacklistMapper.updateById(record);
        }
    }
}
