package com.vls.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vls.reggie.dto.SetmealDto;
import com.vls.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {

    void addWithSetmealDish(SetmealDto setmealDto);

    void removeWithSetmealDish(List<Long> ids);

    SetmealDto getByIdWithDishes(Long id);

    void updateWithDishes(SetmealDto setmealDto);

    void updateStatusByIds(Integer status, List<Long> ids);
}
