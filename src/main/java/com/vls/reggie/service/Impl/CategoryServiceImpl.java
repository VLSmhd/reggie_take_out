package com.vls.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vls.reggie.entity.Category;
import com.vls.reggie.entity.Dish;
import com.vls.reggie.entity.Setmeal;
import com.vls.reggie.exception.CustomException;
import com.vls.reggie.mapper.CategoryMapper;
import com.vls.reggie.service.CategoryService;
import com.vls.reggie.service.DishService;
import com.vls.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前进行判断
     * @param ids
     */
    @Override
    public void remove(Long ids) {
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据category_id查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //抛出一个业务异常
        if(count1 > 0){
            throw new CustomException("当前分类已关联菜品，不能删除");

        }
        //查询当前分类是否关联了套餐,如果已经关联x 抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //抛出一个业务异常
        if(count2 > 0){
            throw new CustomException("当前分类已关联套餐，不能删除");
        }
        //没有关联正常删除
        super.removeById(ids);
    }
}
