package com.example.springbootredis.service;

import com.alibaba.fastjson2.JSON;
import com.example.springbootredis.entity.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class CommandService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String SATELLITE_COMMANDS_PREFIX = "SAT:CMD:";

    public PageResult<Command> queryCommands(List<String> satelliteIds, int pageNum, int pageSize) {
        List<Command> allCommands = new ArrayList<>();

        for (String satId : satelliteIds) {
            String key = SATELLITE_COMMANDS_PREFIX + satId;
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            for (Object json : entries.values()) {
                Command cmd = JSON.parseObject((String) json, Command.class);
                allCommands.add(cmd);
            }
        }

        // ✅ 可在这里添加条件过滤，比如状态、时间段
        // allCommands = allCommands.stream()
        //        .filter(c -> c.getStatus().equals("待执行"))
        //        .collect(Collectors.toList());

        // ✅ 排序（可选）
        allCommands.sort(Comparator.comparing(Command::getCreateTime).reversed());

        // ✅ 分页逻辑
        int total = allCommands.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Command> pageList = total > 0 ? allCommands.subList(fromIndex, toIndex) : List.of();

        return new PageResult<>(pageList, total, pageSize, pageNum);
    }

    public static class PageResult<T> {
        public List<T> records;
        public long total;
        public int size;
        public int current;
        public long pages;

        public PageResult(List<T> records, long total, int size, int current) {
            this.records = records;
            this.total = total;
            this.size = size;
            this.current = current;
            this.pages = (long) Math.ceil((double) total / size);
        }
    }
}