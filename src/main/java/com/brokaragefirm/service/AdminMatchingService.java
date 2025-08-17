package com.brokaragefirm.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brokaragefirm.api.Error.Errors.BadRequestException;
import com.brokaragefirm.api.Error.Errors.NotFoundException;
import com.brokaragefirm.domain.Asset;
import com.brokaragefirm.domain.Order;
import com.brokaragefirm.domain.OrderStatus;
import com.brokaragefirm.domain.Side;
import com.brokaragefirm.repository.AssetRepository;
import com.brokaragefirm.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMatchingService {
  private final OrderRepository orderRepo;
  private final AssetRepository assetRepo;

  /**
   * Matches an order by its ID, updating the asset balances accordingly.
   * 
   * @param orderId the ID of the order to match
   * @return the matched Order with updated status
   */
  private Asset getOrCreate(UUID customerId, String assetName) {
    return assetRepo.findByCustomerIdAndAssetName(customerId, assetName)
        .orElseGet(() -> assetRepo.save(Asset.builder()
            .customerId(customerId).assetName(assetName)
            .size(BigDecimal.ZERO.setScale(Money.SIZE_SCALE))
            .usableSize(BigDecimal.ZERO.setScale(Money.SIZE_SCALE)).build()));
  }

  /**
   * Matches an order by its ID, updating the asset balances accordingly.
   * 
   * @param orderId the ID of the order to match
   * @return the matched Order with updated status
   */
  @Transactional
  public Order matchOrder(UUID orderId) {
    Order o = orderRepo.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
    if (o.getStatus() != OrderStatus.PENDING)
      throw new BadRequestException("Only PENDING can be matched");

    if (o.getOrderSide() == Side.BUY) {
      // Spend reserved TRY (usable was already reduced at create time)
      Asset tryAsset = getOrCreate(o.getCustomerId(), OrderService.TRY);
      BigDecimal cost = Money.cost(o.getPrice(), o.getSize()); // scale = PRICE_SCALE
      // Decrease total TRY size by cost (usable already reserved)
      tryAsset.setSize(tryAsset.getSize().setScale(Money.PRICE_SCALE, Money.RM).subtract(cost));
      assetRepo.save(tryAsset);

      // Deliver shares
      Asset asset = getOrCreate(o.getCustomerId(), o.getAssetName());
      BigDecimal qty = o.getSize().setScale(Money.SIZE_SCALE, Money.RM);
      asset.setSize(asset.getSize().setScale(Money.SIZE_SCALE, Money.RM).add(qty));
      asset.setUsableSize(asset.getUsableSize().setScale(Money.SIZE_SCALE, Money.RM).add(qty));
      assetRepo.save(asset);

    } else { // SELL
      // Reduce total shares (usable was already reserved at create time)
      Asset asset = getOrCreate(o.getCustomerId(), o.getAssetName());
      BigDecimal qty = o.getSize().setScale(Money.SIZE_SCALE, Money.RM);
      asset.setSize(asset.getSize().setScale(Money.SIZE_SCALE, Money.RM).subtract(qty));
      assetRepo.save(asset);

      // Credit TRY
      Asset tryAsset = getOrCreate(o.getCustomerId(), OrderService.TRY);
      BigDecimal proceeds = Money.cost(o.getPrice(), o.getSize());
      tryAsset.setSize(tryAsset.getSize().setScale(Money.PRICE_SCALE, Money.RM).add(proceeds));
      tryAsset.setUsableSize(tryAsset.getUsableSize().setScale(Money.PRICE_SCALE, Money.RM).add(proceeds));
      assetRepo.save(tryAsset);
    }

    o.setStatus(OrderStatus.MATCHED);
    return orderRepo.save(o);
  }
}
