package com.brokaragefirm.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.brokaragefirm.api.Error.GlobalExceptionHandler;
import com.brokaragefirm.api.controller.AssetController;
import com.brokaragefirm.config.AuthContext;
import com.brokaragefirm.domain.Asset;
import com.brokaragefirm.repository.AssetRepository;

@WebMvcTest(controllers = AssetController.class)
@Import(GlobalExceptionHandler.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AssetRepository assetRepository;
    @MockBean
    private AuthContext authContext;

    @Test
    void list_own_assets_ok() throws Exception {
        UUID caller = UUID.randomUUID();

        when(authContext.isAdmin(any())).thenReturn(false);
        when(authContext.currentCustomerId(any())).thenReturn(caller);

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(caller);
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(new BigDecimal("1000.00"));
        tryAsset.setUsableSize(new BigDecimal("800.00"));

        when(assetRepository.findAllByCustomerId(eq(caller))).thenReturn(List.of(tryAsset));

        mvc.perform(get("/api/assets")
                .with(user("user1").roles("CUSTOMER"))
                .param("customerId", caller.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assetName").value("TRY"))
                .andExpect(jsonPath("$[0].size").value(1000.0))
                .andExpect(jsonPath("$[0].usableSize").value(800.0));
    }

    @Test
    void list_someone_else_forbidden() throws Exception {
        UUID caller = UUID.randomUUID();

        when(authContext.isAdmin(any())).thenReturn(false);
        when(authContext.currentCustomerId(any())).thenReturn(caller);

        mvc.perform(get("/api/assets")
                .with(user("user1").roles("CUSTOMER"))
                .param("customerId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_filters_by_assetName() throws Exception {
        UUID caller = UUID.randomUUID();
        when(authContext.isAdmin(any())).thenReturn(false);
        when(authContext.currentCustomerId(any())).thenReturn(caller);

        var tryAsset = new Asset();
        tryAsset.setCustomerId(caller);
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(new BigDecimal("1000.00"));
        tryAsset.setUsableSize(new BigDecimal("800.00"));
        var INGA = new Asset();
        INGA.setCustomerId(caller);
        INGA.setAssetName("INGA");
        INGA.setSize(new BigDecimal("5.0000"));
        INGA.setUsableSize(new BigDecimal("5.0000"));

        when(assetRepository.findAllByCustomerId(eq(caller)))
                .thenReturn(java.util.List.of(tryAsset, INGA));

        mvc.perform(get("/api/assets")
                .with(user("user1").roles("CUSTOMER"))
                .param("customerId", caller.toString())
                .param("assetName", "INGA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assetName").value("INGA"));
    }

    @Test
    void admin_can_list_others_assets() throws Exception {
        UUID target = UUID.randomUUID();
        when(authContext.isAdmin(any())).thenReturn(true); 

        var asset = new Asset();
        asset.setCustomerId(target);
        asset.setAssetName("TRY");
        asset.setSize(new BigDecimal("1000.00"));
        asset.setUsableSize(new BigDecimal("1000.00"));
        when(assetRepository.findAllByCustomerId(eq(target))).thenReturn(java.util.List.of(asset));

        mvc.perform(get("/api/assets")
                .with(user("admin").roles("ADMIN"))
                .param("customerId", target.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assetName").value("TRY"));
    }

    @Test
    void list_returns_empty_when_no_assets() throws Exception {
        UUID caller = UUID.randomUUID();
        when(authContext.isAdmin(any())).thenReturn(false);
        when(authContext.currentCustomerId(any())).thenReturn(caller);
        when(assetRepository.findAllByCustomerId(eq(caller))).thenReturn(java.util.List.of());

        mvc.perform(get("/api/assets")
                .with(user("user1").roles("CUSTOMER"))
                .param("customerId", caller.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

}
