package com.example.springbootredis.service.state.impl;

import com.example.springbootredis.service.state.A;
import org.springframework.stereotype.Component;


@Component
public class Ab extends A {
    @Override
    public void handler() {
        System.out.println("a");
        c.handler();
    }
}
