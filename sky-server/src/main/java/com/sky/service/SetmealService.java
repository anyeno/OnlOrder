package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface SetmealService {
    public SetmealVO getSetmeal(Long id);

    void addSetmeal(SetmealDTO setmealDTO);

    void deleteStemeal(List<Long> ids);

    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    void update(SetmealDTO setmealDTO);

    void status(Integer status, Long id);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
