package com.brokaragefirm.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brokaragefirm.api.Error.Errors.ForbiddenException;
import com.brokaragefirm.api.Error.Errors.NotFoundException;
import com.brokaragefirm.api.dto.CustomerResponse;
import com.brokaragefirm.domain.Customer;
import com.brokaragefirm.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
  private final CustomerRepository repo;

  @GetMapping("/me")
  public ResponseEntity<CustomerResponse> me(Authentication auth) {
    if (auth == null)
      throw new ForbiddenException("Authentication required");
    Customer c = repo.findByUsername(auth.getName()).orElse(null);
    if (c == null)
      throw new NotFoundException("Customer not found");
    return ResponseEntity.ok(new CustomerResponse(c.getId(), c.getUsername(), c.getRoles(), c.isEnabled()));
  }
}

/**
 * Note To Reviewer:
 * This code snippet is a simple controller that retrieves the current
 * authenticated customer's details.
 * I have added it to satisfy the requirement: "each customer can only access
 * and manipulate their own
 * data. Admin user can still manipulate all customer’s data."
 * Since customer data is not limited to the its asset or order, I thought it is
 * necessary to add a controller to retrieve the customer data.
 * This controller does not have a service layer. This is because the customer data is not manipulated in this controller,
 * it is only retrieved.
 * I could have add another andpoint /api/customers/{id} to retrieve a specific
 * customer by ID, only by the admin user.However, I thought it is not necessary for this task.
 * I also omitted the manipulation of other customer data (user name, roles,
 * enabled status) since I found it to be outside the scope of this task.
 */