package com.example.demo.services;

import com.example.demo.dtos.CartItemDTO;
import com.example.demo.dtos.OrderDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.*;
import com.example.demo.reposistories.OrderDetailReposistory;
import com.example.demo.reposistories.OrderReposistory;
import com.example.demo.reposistories.ProductReposistory;
import com.example.demo.reposistories.UserReposistory;
import com.example.demo.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final UserReposistory userReposistory;
    private final OrderReposistory orderReposistory;
    private final OrderDetailReposistory orderDetailRepository;
    private final ProductReposistory productRepository;
    private final ModelMapper modelMapper;

    @Override
    public Order createOrder(OrderDTO orderDTO) {
        // Kiểm tra userId có tồn tại
        User user = userReposistory
                .findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + orderDTO.getUserId()));

        // Ánh xạ DTO -> Entity
        Order order = modelMapper.map(orderDTO, Order.class);
        order.setId(null); // Đảm bảo ID là null để Hibernate tự sinh
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);

        // Kiểm tra shipping date >= hôm nay
        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if (shippingDate.isBefore(ChronoLocalDate.from(LocalDateTime.now()))) {
            throw new DataNotFoundException("Shipping date must be greater than today");
        }

        order.setShippingAddress(orderDTO.getShippingAddress());
        System.out.println("Shipping address: " + order.getShippingAddress());
        order.setShippingDate(shippingDate);
        order.setActive(true);
        orderReposistory.save(order);
        // Tạo danh sách các đối tượng OrderDetail từ cartItems
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
            // Tạo một đối tượng OrderDetail từ CartItemDTO
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);

            // Lấy thông tin sản phẩm từ cartItemDTO
            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();

            // Tìm thông tin sản phẩm từ cơ sở dữ liệu (hoặc sử dụng cache nếu cần)
            Products product = productRepository.findById(productId)
                    .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + productId));

            // Đặt thông tin cho OrderDetail
            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(quantity);
            // Các trường khác của OrderDetail nếu cần
            orderDetail.setTotalPrice(product.getPrice() * quantity);

            // Thêm OrderDetail vào danh sách
            orderDetails.add(orderDetail);
        }

        // Lưu danh sách OrderDetail vào cơ sở dữ liệu
        orderDetailRepository.saveAll(orderDetails);
        return order;
    }

    @Override
    public Order getOrder(long id) {
        return orderReposistory.findById(id).orElseThrow(() ->
                new DataNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional
    public Order updateOrder(long Id, OrderDTO orderDTO) {
        Order existingOrder = orderReposistory.findById(Id).orElseThrow(() ->
                new DataNotFoundException("Order not found with id: " + Id));
        User existingUser = userReposistory.findById(orderDTO.getUserId()).orElseThrow(() ->
                new DataNotFoundException("User not found with id: " + Id));
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        modelMapper.map(orderDTO, existingOrder);
        existingOrder.setUser(existingUser);
        return orderReposistory.save(existingOrder);
    }

    @Override
    public void deleteOrder(long id) {
        Order order = orderReposistory.findById(id).orElse(null);
        if (order != null) {
            order.setActive(false);
            orderReposistory.save(order);
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderReposistory.findByUserId(userId);
    }
}
