package com.brokaragefirm.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.brokaragefirm.domain.Order;
import com.brokaragefirm.domain.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, java.util.UUID> {

  /**
   * Finds all orders for a given customer within a date range.
   * 
   * @param customerId the ID of the customer
   * @param from       the start date of the range (inclusive)
   * @param to         the end date of the range (inclusive)
   * @param p          pagination information
   * @return a Page of Orders matching the criteria
   */
  Page<Order> findAllByCustomerIdAndCreateDateBetween(java.util.UUID customerId, Instant from, Instant to, Pageable p);

  /**
   * Finds all orders for a given customer with pagination.
   * 
   * @param customerId the ID of the customer
   * @param p          pagination information
   * @return a Page of Orders for the customer
   */
  Page<Order> findAllByCustomerId(java.util.UUID customerId, Pageable p);

  /**
   * Finds an order by its ID and customer ID.
   * 
   * @param id         the ID of the order
   * @param customerId the ID of the customer who owns the order
   * @return an Optional containing the Order if found, or empty if not found
   */
  Optional<Order> findByIdAndCustomerId(java.util.UUID id, java.util.UUID customerId);

  /**
   * Finds all orders for a given customer with a specific status.
   * 
   * @param customerId the ID of the customer
   * @param status     the status of the orders to filter by
   * @param p          pagination information
   * @return a Page of Orders matching the criteria
   */
  Page<Order> findAllByCustomerIdAndStatus(java.util.UUID customerId, OrderStatus status, Pageable p);
}
