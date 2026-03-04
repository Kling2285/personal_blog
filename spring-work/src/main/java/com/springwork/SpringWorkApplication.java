package com.springwork;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.springwork.mapper")
public class SpringWorkApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpringWorkApplication.class, args);
    }

}
