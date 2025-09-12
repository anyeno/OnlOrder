package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShoppingCartService {
    void addCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> list();

    void clean();

    void removeCart(ShoppingCartDTO shoppingCartDTO);
}
