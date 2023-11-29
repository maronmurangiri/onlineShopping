package com.maron.orderservice.controller;

import com.maron.orderservice.dto.OrderRequest;
import com.maron.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {


   private final  OrderService orderService;
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest){
        try{
            return ResponseEntity.ok(orderService.placeOrder(orderRequest));
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.ok(e.getMessage());
        }

    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(){

        return  ResponseEntity.ok(orderService.getAllOrders());
    }

}
