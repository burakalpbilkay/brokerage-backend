package com.brokaragefirm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brokaragefirm.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, java.util.UUID> {

  /**
   * Finds a customer by their username.
   * 
   * @param username the unique username of the customer
   * @return an Optional containing the Customer if found, or empty if not found
   */
  Optional<Customer> findByUsername(String username);
}
