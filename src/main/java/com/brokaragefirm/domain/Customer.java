package com.brokaragefirm.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(name = "uk_customer_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
  @Id
  @UuidGenerator
  private UUID id;

  @Column(nullable = false, unique = true)
  private String username; // unique username for login

  @Column(nullable = false)
  private String password; // hashed password

  @Column(nullable = false)
  private String roles; // comma-separated roles, e.g. "ROLE_CUSTOMER,ROLE_ADMIN"

  @Column(nullable = false)
  private boolean enabled; // whether the account is active

  @Column(updatable = false)
  private Instant createdAt;

  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    createdAt = Instant.now();
    updatedAt = createdAt;
    if (roles == null)
      roles = "ROLE_CUSTOMER";
    enabled = true;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
