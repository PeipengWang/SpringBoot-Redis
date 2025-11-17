package com.example.springbootredis.service.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class A {
    @Autowired
    public C c;

    public void handler(){

    }
}
