package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;


    @GetMapping("{id}")
    public Result getSetmeal(@PathVariable Long id) {
        log.info("getSeteal by id:{}", id);
        SetmealVO setmealVO = setmealService.getSetmeal(id);
        return Result.success(setmealVO);
    }

    @PostMapping()
    @CacheEvict(cacheNames = "setmeal", key = "#setmealDTO.categoryId")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("addSetmeal {}", setmealDTO);
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    @DeleteMapping()
    public Result deleteSetmeal(@RequestParam List<Long> ids) {
        log.info("deleteSetmeal {}", ids);
        setmealService.deleteStemeal(ids);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("pageSetmeal {}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping()
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("update setmeal {}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result status(@PathVariable Integer status, @RequestParam Long id){
        log.info("status setmeal {}", status);
        setmealService.status(status, id);
        return Result.success();
    }
}
