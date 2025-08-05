package com.example.demo.services;

import com.example.demo.dtos.OrderDTO;
import com.example.demo.dtos.OrderDetailDTO;
import com.example.demo.models.OrderDetail;

import java.util.List;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO);

    OrderDetail getOrderDetail(long id);

    OrderDetail updateOrderDetail(long orderId, OrderDetailDTO orderDetailDTO);

    void deleteOrderDetail(long id);

    List<OrderDetail> findByOrderId(Long orderId);
}
