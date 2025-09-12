package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 根据id查询套餐相关信息
     * @param id
     * @return
     */
    public SetmealVO getSetmeal(Long id) {
        // 根据套餐id查询套餐
        Setmeal setmeal = setmealMapper.getById(id);
        // 根据套餐id查询套餐-菜品
        List<SetmealDish> dishes = setmealDishMapper.getBySetmealId(id);
        // 封装vo
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(dishes);
        return setmealVO;
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        // 插入套餐表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        // 插入套餐-菜品表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && setmealDishes.size() > 0) {
            for (int i = 0; i < setmealDishes.size(); i++) {
                SetmealDish setmealDish = setmealDishes.get(i);
                setmealDish.setSetmealId(setmealId);
                setmealDishMapper.insert(setmealDish);
            }
        }
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    public void deleteStemeal(List<Long> ids) {
        // 判断套餐是否能删除
        for(Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        // 删除套餐表和套餐-菜品关联表中的数据
        for(Long id : ids) {
            setmealMapper.deleteById(id);
            setmealDishMapper.deleteBySetmealId(id);
        }

    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);//后绪步骤实现
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //修改套餐表基本信息
        setmealMapper.update(setmeal);

        //删除原有的菜品数据
        setmealDishMapper.deleteBySetmealId(setmeal.getId());

        //重新插入套餐-菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            });
            //向套餐-菜品表插入n条数据
            setmealDishMapper.insertBatch(setmealDishes);

        }
    }

    /**
     * 修改套餐起售停售状态
     * @param status
     * @param id
     */
    public void status(Integer status, Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
