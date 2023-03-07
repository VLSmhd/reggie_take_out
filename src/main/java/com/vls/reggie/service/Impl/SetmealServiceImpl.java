package com.vls.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vls.reggie.dto.SetmealDto;
import com.vls.reggie.entity.Setmeal;
import com.vls.reggie.entity.SetmealDish;
import com.vls.reggie.exception.CustomException;
import com.vls.reggie.mapper.SetmealMapper;
import com.vls.reggie.service.SetmealDishService;
import com.vls.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 添加套餐，套餐——菜品
     * @param setmealDto
     */
    @Override
    @Transactional
    public void addWithSetmealDish(SetmealDto setmealDto) {
        //添加套餐信息
        this.save(setmealDto);//insert之后，这个套餐就有自动生成的id了


        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //需要获取setmealId才能往setmealDish表里添加
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐、套餐——菜品关联
     * @param ids
     */
    @Override
    public void removeWithSetmealDish(List<Long> ids) {
        //查询套餐状态,可以删，不可以抛出业务异常
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(ids!=null, Setmeal::getId,ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(lambdaQueryWrapper);
        if(count > 0){
            throw new CustomException("套餐售卖ing ， 不能删除");
        }

        //删除setmeal表里数据
        this.removeByIds(ids);

        //删除setmealDish表里数据
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);

    }


    /**
     * 通过套餐id查询套餐信息和菜品信息
     * @param id
     */
    @Override
    @Transactional
    public SetmealDto getByIdWithDishes(Long id) {
        //获取菜品信息
        Setmeal setmeal = this.getById(id);

        //根据setmeal的id查询菜品信息
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(id != null, SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(dishLambdaQueryWrapper);

        SetmealDto setmealDto = new SetmealDto();
        setmealDto.setSetmealDishes(setmealDishes);

        BeanUtils.copyProperties(setmeal, setmealDto);

        return setmealDto;
    }

    /**
     * 修改套餐 以及  菜品
     * @param setmealDto
     * @return
     */
    @Override
    @Transactional
    public void updateWithDishes(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        //删除原先setmeal_dish表的信息
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lambdaQueryWrapper);

        //添加新信息：
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishService.saveBatch(setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList()));

    }


    /**
     * 通过ids批量修改售卖状态
     * @param status
     * @param ids
     */
    @Override
    public void updateStatusByIds(Integer status, List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmeal = this.getById(id);
            if(!setmeal.getStatus().equals(status)){
                setmeal.setStatus(status);
                this.updateById(setmeal);
            }
        }
    }
}
