package com.example.demo.services;

import com.example.demo.dtos.OrderDTO;
import com.example.demo.models.Order;

import java.util.List;

public interface IOrderService {
    Order createOrder(OrderDTO orderDTO);

    Order getOrder(long id);

    Order updateOrder(long orderId, OrderDTO orderDTO);

    void deleteOrder(long id);

    List<Order> findByUserId(Long userId);
}
