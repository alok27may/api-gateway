package com.kumaral.apigateway;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("car-service")
public interface CarClient {

    @GetMapping("/cars")
    @CrossOrigin
    CollectionModel<Car> readCars();
}
