package com.brokaragefirm.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.brokaragefirm.api.Error.GlobalExceptionHandler;
import com.brokaragefirm.api.controller.AdminOrderController;
import com.brokaragefirm.config.AuthContext;
import com.brokaragefirm.domain.Order;
import com.brokaragefirm.domain.OrderStatus;
import com.brokaragefirm.domain.Side;
import com.brokaragefirm.service.AdminMatchingService;

@WebMvcTest(controllers = AdminOrderController.class)
@Import(GlobalExceptionHandler.class) // only the exception mapper
class AdminOrderControllerTest {

  @Autowired private MockMvc mvc;

  // Mock collaborators used by the controller
  @MockBean private AdminMatchingService admin;
  @MockBean private AuthContext auth;

  @Test
  void match_as_admin_returns_200_and_body() throws Exception {
    UUID oid = UUID.randomUUID();

    // Controller-level auth check
    when(auth.isAdmin(any(Authentication.class))).thenReturn(true);

    // Service returns a matched order
    var matched = new Order();
    matched.setId(oid);
    matched.setCustomerId(UUID.randomUUID());
    matched.setAssetName("INGA");
    matched.setOrderSide(Side.BUY);
    matched.setSize(new BigDecimal("1.0000"));
    matched.setPrice(new BigDecimal("10.00"));
    matched.setStatus(OrderStatus.MATCHED);
    when(admin.matchOrder(eq(oid))).thenReturn(matched);

    mvc.perform(post("/api/admin/orders/{id}/match", oid)
            .with(user("admin").roles("ADMIN")) // principal for security context
            .with(csrf())                        // POST ⇒ include CSRF when SecurityConfig isn’t loaded
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(oid.toString()))
        .andExpect(jsonPath("$.status").value("MATCHED"));
  }

  @Test
  void non_admin_forbidden() throws Exception {
    // Controller-level auth check
    when(auth.isAdmin(any(Authentication.class))).thenReturn(false);

    mvc.perform(post("/api/admin/orders/{id}/match", UUID.randomUUID())
            .with(user("user1").roles("CUSTOMER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }
}
