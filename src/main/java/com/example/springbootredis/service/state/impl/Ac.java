package com.example.springbootredis.service.state.impl;

import com.example.springbootredis.service.state.A;
import org.springframework.stereotype.Component;

@Component
public class Ac extends A {
    public void handler(){
        System.out.println("b");
        c.handler();
    }
}
