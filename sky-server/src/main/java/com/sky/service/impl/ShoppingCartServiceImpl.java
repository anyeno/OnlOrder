package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addCart(ShoppingCartDTO shoppingCartDTO) {
        // 判断该商品是否已经存在购物车 dishId+dishFlavor+userId
        // 补充缺失的属性 加入购物车表
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart select_cart = shoppingCartMapper.selectBy(shoppingCart);
        if (select_cart == null) { // 购物车没有该商品
            // 如果添加的是菜品
            if(shoppingCartDTO.getDishId() != null) {
                Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            }else { // 添加的是套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            // 存入购物车表中
            shoppingCartMapper.insert(shoppingCart);
        }else { // 购物车已有该商品
            select_cart.setNumber(select_cart.getNumber() + 1);
            shoppingCartMapper.update(select_cart);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectByUserId(BaseContext.getCurrentId());
        return shoppingCarts;
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        shoppingCartMapper.deleteByUderId(BaseContext.getCurrentId());
    }

    /**
     * 移出购物车
     * @param shoppingCartDTO
     */
    @Override
    public void removeCart(ShoppingCartDTO shoppingCartDTO) {
        // 判断该商品是否已经存在购物车 dishId+dishFlavor+userId
        // 不存在的话直接返回
        // num=1 直接删除
        // num>1 num-1
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart select_cart = shoppingCartMapper.selectBy(shoppingCart);
        if (select_cart != null) { // 购物车有该商品
            log.info("有该商品");
            Integer number = select_cart.getNumber();
            if(number == 1){
                shoppingCartMapper.deleteById(select_cart.getId());
            } else {
                select_cart.setNumber(number - 1);
                shoppingCartMapper.update(select_cart);
            }

        }
    }
}
