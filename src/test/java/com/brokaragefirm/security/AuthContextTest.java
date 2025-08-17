package com.brokaragefirm.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.brokaragefirm.config.AuthContext;
import com.brokaragefirm.domain.Customer;
import com.brokaragefirm.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class AuthContextTest {

    @Test
    void isAdmin_true_for_role_admin() {
        var auth = new TestingAuthenticationToken("admin", "x",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        var ctx = new AuthContext(Mockito.mock(CustomerRepository.class));
        assertThat(ctx.isAdmin(auth)).isTrue();
    }

    @Test
    void currentCustomerId_looks_up_by_username() {
        var repo = Mockito.mock(CustomerRepository.class);
        var ctx = new AuthContext(repo);

        var c = new Customer();
        c.setUsername("user1");
        var id = UUID.randomUUID();
        try {
            var idField = Customer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(c, id);
        } catch (Exception ignored) {
        }

        Mockito.when(repo.findByUsername("user1")).thenReturn(Optional.of(c));

        var auth = new TestingAuthenticationToken("user1", "pw");
        assertThat(ctx.currentCustomerId(auth)).isEqualTo(id);
    }
}
