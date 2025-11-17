package com.example.springbootredis;

import com.example.springbootredis.service.state.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class TestAbstract {

    @Autowired
    public Map<String, A> map = new HashMap<>();
    @Test
    public void test(){
        map.keySet().forEach(key -> {
           map.get(key).handler();
        });
    }
}
