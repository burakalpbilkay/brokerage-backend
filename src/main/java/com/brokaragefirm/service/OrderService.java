
package com.brokaragefirm.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brokaragefirm.api.Error.Errors.BadRequestException;
import com.brokaragefirm.api.Error.Errors.InsufficientBalanceException;
import com.brokaragefirm.api.Error.Errors.NotFoundException;
import com.brokaragefirm.api.dto.CreateOrderRequest;
import com.brokaragefirm.api.dto.OrderFilterRequest;
import com.brokaragefirm.domain.Asset;
import com.brokaragefirm.domain.Order;
import com.brokaragefirm.domain.OrderStatus;
import com.brokaragefirm.domain.Side;
import com.brokaragefirm.repository.AssetRepository;
import com.brokaragefirm.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
  public static final String TRY = "TRY";
  private final AssetRepository assetRepo;
  private final OrderRepository orderRepo;

  private Asset getOrCreate(UUID customerId, String assetName) {
    return assetRepo.findByCustomerIdAndAssetName(customerId, assetName)
        .orElseGet(() -> assetRepo.save(Asset.builder()
            .customerId(customerId)
            .assetName(assetName)
            .size(BigDecimal.ZERO.setScale(Money.SIZE_SCALE))
            .usableSize(BigDecimal.ZERO.setScale(Money.SIZE_SCALE))
            .build()));
  }

  
  @Transactional
  public Order createOrder(CreateOrderRequest req) {
    if (TRY.equalsIgnoreCase(req.getAssetName()))
      throw new BadRequestException("Trading TRY against TRY is not allowed");
    // Reserve balances
    int retries = 3;
    while (true) {
      try {
        if (req.getSide() == Side.BUY) {
          Asset tryAsset = getOrCreate(req.getCustomerId(), TRY);
          BigDecimal needed = Money.cost(req.getPrice(), req.getSize());
          if (tryAsset.getUsableSize().setScale(Money.PRICE_SCALE, Money.RM).compareTo(needed) < 0) {
            throw new InsufficientBalanceException("Insufficient TRY usable balance");
          }
          tryAsset.setUsableSize(tryAsset.getUsableSize().setScale(Money.PRICE_SCALE, Money.RM).subtract(needed));
          assetRepo.saveAndFlush(tryAsset);
        } else { // SELL
          Asset asset = getOrCreate(req.getCustomerId(), req.getAssetName());
          if (asset.getUsableSize().compareTo(req.getSize()) < 0) {
            throw new InsufficientBalanceException("Insufficient asset usable shares");
          }
          asset.setUsableSize(asset.getUsableSize().subtract(req.getSize().setScale(Money.SIZE_SCALE, Money.RM)));
          assetRepo.saveAndFlush(asset);
        }
        Order order = Order.builder()
            .customerId(req.getCustomerId())
            .assetName(req.getAssetName())
            .orderSide(req.getSide())
            .size(req.getSize().setScale(Money.SIZE_SCALE, Money.RM))
            .price(req.getPrice().setScale(Money.PRICE_SCALE, Money.RM))
            .status(OrderStatus.PENDING)
            .createDate(Instant.now())
            .build();
        return orderRepo.save(order);
      } catch (OptimisticLockingFailureException e) {
        if (--retries <= 0)
          throw e;
      }
    }
  }

  public Page<Order> listOrders(OrderFilterRequest f) {
    Instant from = Optional.ofNullable(f.getFrom()).orElse(Instant.EPOCH);
    Instant to = Optional.ofNullable(f.getTo()).orElse(Instant.now());
    Pageable p = PageRequest.of(
        Optional.ofNullable(f.getPage()).orElse(0),
        Optional.ofNullable(f.getSize()).orElse(20),
        Sort.by(Sort.Direction.DESC, "createDate"));
    Page<Order> page = orderRepo.findAllByCustomerIdAndCreateDateBetween(f.getCustomerId(), from, to, p);
    if (f.getStatus() != null) {
      page = new PageImpl<>(page.getContent().stream().filter(o -> o.getStatus() == f.getStatus()).toList(), p,
          page.getTotalElements());
    }
    if (f.getAssetName() != null && !f.getAssetName().isBlank()) {
      page = new PageImpl<>(
          page.getContent().stream().filter(o -> f.getAssetName().equalsIgnoreCase(o.getAssetName())).toList(), p,
          page.getTotalElements());
    }
    return page;
  }

  @Transactional
  public void cancelOrder(UUID orderId) {
    Order o = orderRepo.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
    if (o.getStatus() != OrderStatus.PENDING)
      throw new BadRequestException("Only PENDING orders can be canceled");

    if (o.getOrderSide() == Side.BUY) {
      Asset tryAsset = getOrCreate(o.getCustomerId(), TRY);
      BigDecimal refund = Money.cost(o.getPrice(), o.getSize());
      tryAsset.setUsableSize(tryAsset.getUsableSize().add(refund));
      assetRepo.save(tryAsset);
    } else {
      Asset asset = getOrCreate(o.getCustomerId(), o.getAssetName());
      asset.setUsableSize(asset.getUsableSize().add(o.getSize()));
      assetRepo.save(asset);
    }
    o.setStatus(OrderStatus.CANCELED);
    orderRepo.save(o);
  }
}
