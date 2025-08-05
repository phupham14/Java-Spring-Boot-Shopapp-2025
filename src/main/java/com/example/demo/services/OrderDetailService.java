package com.example.demo.services;

import com.example.demo.dtos.OrderDetailDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.Order;
import com.example.demo.models.OrderDetail;
import com.example.demo.models.Products;
import com.example.demo.reposistories.OrderDetailReposistory;
import com.example.demo.reposistories.OrderReposistory;
import com.example.demo.reposistories.ProductReposistory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService {
    private final OrderReposistory orderReposistory;
    private final OrderDetailReposistory orderDetailReposistory;
    private final ProductReposistory productReposistory;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        // Ktra xem orderId có tồn tại hay k
        Order order = orderReposistory.findById(Long.valueOf(orderDetailDTO.getOrderId()))
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find Order with orderId: " + orderDetailDTO.getOrderId()));
        // Tìm product id trong ds product
        Products products = productReposistory.findById(Long.valueOf(orderDetailDTO.getProductId()))
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find Product with productId: " + orderDetailDTO.getProductId()));

        Products product = productReposistory.findById(products.getId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        // Lấy giá sản phẩm từ DB
        Float productPrice = product.getPrice();

        // Tính tổng tiền
        float totalMoney = productPrice * orderDetailDTO.getNumberOfProducts();

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(products)
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .color(orderDetailDTO.getColor())
                .totalPrice(totalMoney)
                .build();

        // Lưu vào DB và trả về
        return orderDetailReposistory.save(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetail(long id) throws DataNotFoundException {
        return orderDetailReposistory.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find OrderDetail with id: " + id));
    }

    @Override
    public OrderDetail updateOrderDetail(long orderId, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        // Tìm xem orderDetail có tồn tại hay k
        OrderDetail existingOrderDetail = orderDetailReposistory.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Cannot find OrderDetail with id: " + orderId));
        Order existingOrder = orderReposistory.findById(Long.valueOf(orderDetailDTO.getOrderId()))
                .orElseThrow(() -> new DataNotFoundException("Cannot find Order with id: " + orderId));
        // Tìm xem product có tồn tại k
        Products products = productReposistory.findById(Long.valueOf(orderDetailDTO.getProductId()))
                .orElseThrow(() -> new DataNotFoundException("Cannot find Product with id: " + orderId));

        Products product = productReposistory.findById(products.getId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        // Cập nhật số lượng và màu
        existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
        existingOrderDetail.setColor(orderDetailDTO.getColor());

        // Cập nhật lại tổng tiền
        float totalMoney = product.getPrice() * orderDetailDTO.getNumberOfProducts();
        existingOrderDetail.setTotalPrice(totalMoney);

        return orderDetailReposistory.save(existingOrderDetail);
    }

    @Override
    @Transactional
    public void deleteOrderDetail(long id) {
        orderDetailReposistory.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailReposistory.findByOrderId(orderId);
    }
}
