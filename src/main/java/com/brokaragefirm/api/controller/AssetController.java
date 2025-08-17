package com.brokaragefirm.api.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brokaragefirm.api.Error.Errors.ForbiddenException;
import com.brokaragefirm.api.dto.AssetResponse;
import com.brokaragefirm.config.AuthContext;
import com.brokaragefirm.domain.Asset;
import com.brokaragefirm.repository.AssetRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

  private final AssetRepository assetRepo;
  private final AuthContext auth;

  @GetMapping
  public ResponseEntity<List<AssetResponse>> list(@RequestParam("customerId") UUID customerId,
      @RequestParam(value = "assetName", required = false) String assetName,
      Authentication who) {
    if (!auth.isAdmin(who)) {
      java.util.UUID me = auth.currentCustomerId(who);
      if (me == null || !me.equals(customerId))
        throw new ForbiddenException("Customers can only view their own assets.");
    }
    List<Asset> all = assetRepo.findAllByCustomerId(customerId);
    if (assetName != null && !assetName.isBlank()) {
      all = all.stream().filter(a -> a.getAssetName().equalsIgnoreCase(assetName)).collect(Collectors.toList());
    }
    var resp = all.stream()
        .map(a -> new AssetResponse(a.getId(), a.getCustomerId(), a.getAssetName(), a.getSize(), a.getUsableSize()))
        .toList();
    return ResponseEntity.ok(resp);
  }
}
