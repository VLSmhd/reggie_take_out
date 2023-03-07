package com.vls.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vls.reggie.dto.DishDto;
import com.vls.reggie.entity.Dish;
import com.vls.reggie.entity.DishFlavor;
import com.vls.reggie.exception.CustomException;
import com.vls.reggie.mapper.DishMapper;
import com.vls.reggie.service.DishFlavorService;
import com.vls.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品+口味
     * @param dishDto
     */
    @Override
    //多表操作，开启事务
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息
        this.save(dishDto);
        //保存口味。注意这里前端传过来的是没有dis id的，需要自行获取处理
        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据菜品id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品信息
        Dish dish = this.getById(id);

        //口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);


        DishDto dishDto = new DishDto();
        dishDto.setFlavors(dishFlavors);
        BeanUtils.copyProperties(dish, dishDto);

        return dishDto;
    }


    /**
     * 更新菜品信息及其口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //修改菜品
        this.updateById(dishDto);

        //修改口味
        //先删除原先口味
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //添加新口味
        List<DishFlavor> newFlavors = dishDto.getFlavors();

        //因为dishFlavor里的dishId没有外键关联，需要手动设置添加。多对一关系。多：dishFlavor
        dishFlavorService.saveBatch(newFlavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList()));


    }

    /**
     * 状态批量改变
     * @param status
     * @param ids
     */
    @Override
    //执行多条，开启事务
    @Transactional
    public void updateStatusByIds(Integer status, Long[] ids) {
        for (Long id : ids) {
            //取dish看状态
            Dish dish = this.getById(id);

            //状态不一致就改
            if(!dish.getStatus().equals(status)){
                dish.setStatus(status);
                this.updateById(dish);
            }
        }
    }


    /**
     * 批量删除、删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(ids != null, Dish::getId, ids);
        //只能删不售卖的
        List<Dish> list = this.list(lambdaQueryWrapper);
        for (Dish dish : list) {
            if(dish.getStatus() == 0){
                this.removeById(dish.getId());
            }else{
                throw new CustomException("菜品：\"" + dish.getName() + "\"在售,无法删除");
            }
        }

    }

}
