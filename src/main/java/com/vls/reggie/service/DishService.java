package com.vls.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vls.reggie.dto.DishDto;
import com.vls.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品+口味，操作两张表
    void saveWithFlavor(DishDto dishDto);


    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    void updateStatusByIds(Integer status, Long[] ids);

    void deleteByIds(List<Long> ids);
}
