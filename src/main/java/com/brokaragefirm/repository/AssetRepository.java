package com.brokaragefirm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brokaragefirm.domain.Asset;

public interface AssetRepository extends JpaRepository<Asset, java.util.UUID> {
  /**
   * Finds an asset by its customer ID and asset name.
   * 
   * @param customerId the ID of the customer
   * @param assetName  the name of the asset
   * @return an Optional containing the Asset if found, or empty if not found
   **/
  Optional<Asset> findByCustomerIdAndAssetName(java.util.UUID customerId, String assetName);

  /**
   * Finds all assets for a given customer.
   * 
   * @param customerId the ID of the customer
   * @return a List of Assets owned by the customer
   **/
  List<Asset> findAllByCustomerId(java.util.UUID customerId);
}
