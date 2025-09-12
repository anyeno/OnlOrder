package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags="用户购物车")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result addCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车:{}", shoppingCartDTO);
        shoppingCartService.addCart(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result list() {
        log.info("查询购物车");
        List<ShoppingCart> shoppingCarts = shoppingCartService.list();
        return Result.success(shoppingCarts);
    }

    @DeleteMapping("/clean")
    public Result clean() {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }

    @PostMapping("/sub")
    public Result removeCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("移出购物车");
        shoppingCartService.removeCart(shoppingCartDTO);
        return Result.success();
    }
}
