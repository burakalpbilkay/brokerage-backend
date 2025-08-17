package com.brokaragefirm.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.brokaragefirm.api.Error.GlobalExceptionHandler;
import com.brokaragefirm.api.controller.CustomerController;
import com.brokaragefirm.domain.Customer;
import com.brokaragefirm.repository.CustomerRepository;

@WebMvcTest(controllers = CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

  @Autowired private MockMvc mvc;

  @MockBean private CustomerRepository customerRepository;

  @Test
  void me_returns_customer_profile() throws Exception {
    var c = new Customer();
    var id = UUID.randomUUID();
    c.setUsername("user1");
    c.setRoles("ROLE_CUSTOMER");
    c.setEnabled(true);
    try {
      var f = Customer.class.getDeclaredField("id");
      f.setAccessible(true); f.set(c, id);
    } catch (Exception ignore) {}

    when(customerRepository.findByUsername("user1")).thenReturn(Optional.of(c));

    mvc.perform(get("/api/customers/me").with(user("user1").roles("CUSTOMER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.username").value("user1"))
        .andExpect(jsonPath("$.roles").value("ROLE_CUSTOMER"));
  }
}
