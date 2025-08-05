package com.example.demo.reposistories;

import com.example.demo.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailReposistory extends JpaRepository <OrderDetail, Long>{
    List<OrderDetail> findByOrderId(Long orderId);
}
