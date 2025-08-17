package com.brokaragefirm.config;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.brokaragefirm.domain.Customer;
import com.brokaragefirm.repository.CustomerRepository;

@Component
public class AuthContext {
  private final CustomerRepository customers;

  public AuthContext(CustomerRepository customers) {
    this.customers = customers;
  }

  /**
   * Checks if the authenticated user has admin privileges.
   * Returns true if the user is an admin, false otherwise.
   */
  public boolean isAdmin(Authentication auth) {
    if (auth == null)
      return false;
    Set<String> roles = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    return roles.contains("ROLE_ADMIN");
  }

  /**
   * Returns the current customer's ID if the user is authenticated and not an
   * admin.
   * Returns null if the user is an admin or not authenticated.
   */
  public UUID currentCustomerId(Authentication auth) {
    if (auth == null || isAdmin(auth))
      return null;
    return customers.findByUsername(auth.getName()).map(Customer::getId).orElse(null);
  }
}
