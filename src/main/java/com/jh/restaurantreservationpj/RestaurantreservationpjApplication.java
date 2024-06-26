package com.jh.restaurantreservationpj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableCaching
public class RestaurantreservationpjApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantreservationpjApplication.class, args);
    }

}
