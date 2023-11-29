package com.maron.orderservice.service;

import com.maron.orderservice.dto.InventoryResponse;
import com.maron.orderservice.dto.OrderLineItemsDTO;
import com.maron.orderservice.dto.OrderRequest;
import com.maron.orderservice.model.Order;
import com.maron.orderservice.model.OrderLineItems;
import com.maron.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public WebClient.Builder webClientBuilder;
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());


        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDTOList()
                .stream()
                .map(this::mapToOrderLineItems).toList();

        order.setOrderLineItemsList(orderLineItems);
        List<String> skuCodesList = order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode).toList();

        //check if product exist in the inventory
       InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCodeList",skuCodesList).build())
                .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                                .block();

        assert inventoryResponseArray != null;
        boolean results = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

        if(results){
            orderRepository.save(order);
            return  "Order Placed successfully";
        }else {
            return new IllegalArgumentException("The order out of stock").toString();
        }

    }

    private OrderLineItems mapToOrderLineItems(OrderLineItemsDTO orderLineItem) {
       return OrderLineItems.builder()
                .skuCode(orderLineItem.getSkuCode())
                .quantity(orderLineItem.getQuantity())
                .price(orderLineItem.getPrice())
                .build();

    }

    public List<OrderRequest> getAllOrders() {
        List<Order> orderList = orderRepository.findAll();

         return orderList.stream().map(this::mapToOrderRequestDTO).toList();

    }

    private OrderRequest mapToOrderRequestDTO(Order order) {
        return OrderRequest.builder()
                .orderNumber(order.getOrderNumber())
                .orderLineItemsDTOList(
                        order.getOrderLineItemsList().stream().map(this::mapToOrderLineItemDTO).toList())
                .build();
    }

    private OrderLineItemsDTO mapToOrderLineItemDTO(OrderLineItems orderLineItems) {
       return OrderLineItemsDTO.builder()
                .skuCode(orderLineItems.getSkuCode())
                .quantity(orderLineItems.getQuantity())
                .price(orderLineItems.getPrice())
                .build();
    }
}
