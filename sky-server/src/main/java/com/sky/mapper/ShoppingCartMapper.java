package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 条件查询
     *
     * @param shoppingCart
     * @return
     */
    ShoppingCart selectBy(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            " values (#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);
    /**
     * 更新商品数量
     *
     * @param selectCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void update(ShoppingCart selectCart);

    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> selectByUserId(Long userId);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUderId(Long userId);

    @Delete("delete from shopping_cart where id=#{id}")
    void deleteById(Long id);
}
