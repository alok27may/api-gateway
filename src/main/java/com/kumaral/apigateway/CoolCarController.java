package com.kumaral.apigateway;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("gateway")
public class CoolCarController {
    private final CarClient carClient;

    @Autowired private DiscoveryClient discoveryClient;
    @Autowired private RestTemplate restTemplate;

    public CoolCarController(CarClient carClient) {
        this.carClient = carClient;
    }

    @GetMapping("/cool-cars")
    @CrossOrigin
    @HystrixCommand(fallbackMethod = "fallback")
    public Collection<Car> goodCars() {
        System.out.println(carClient);
        return carClient.readCars()
                .getContent()
                .stream()
                .filter(this::isCool)
                .collect(Collectors.toList());
    }

    @GetMapping("/cool-cars/{name}")
    public Car getCarByName(@PathVariable final String name) {
        final List<ServiceInstance> instances = discoveryClient.getInstances("car-service");
        final String url = instances.get(0).getUri().toString();

        final RestTemplate restTemplateTest = new RestTemplate();
        final ResponseEntity<Car> exchange =
                restTemplateTest.exchange(url + "/car/cars/{name}",
                        HttpMethod.GET, null,
                        Car.class, name);

        return exchange.getBody();
    }

    @GetMapping("/car-ribbon/{name}")
    public Car getRibbonBasedCarByName(@PathVariable final String name) {
        final ResponseEntity<Car> exchange =
                restTemplate.exchange("http://car-service/car/cars/{name}",
                        HttpMethod.GET, null,
                        Car.class, name);

        return exchange.getBody();
    }

    @GetMapping("/msg")
    public String getGatewayMsg() {
        return "Getting message from Api-Gateway";
    }

    private Collection<Car> fallback() {
        final Car testCar = new Car();
        testCar.setName("TestCar");

        final ArrayList<Car> cars = new ArrayList<>();
        cars.add(testCar);
        return cars;
    }

    private boolean isCool(Car car) {
        return !car.getName().equals("AMC Gremlin") &&
                !car.getName().equals("Triumph Stag") &&
                !car.getName().equals("Ford Pinto") &&
                !car.getName().equals("Yugo GV");
    }
}
