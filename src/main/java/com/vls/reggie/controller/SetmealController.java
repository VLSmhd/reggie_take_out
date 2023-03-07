package com.vls.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vls.reggie.common.R;
import com.vls.reggie.dto.SetmealDto;
import com.vls.reggie.entity.Category;
import com.vls.reggie.entity.Setmeal;
import com.vls.reggie.service.CategoryService;
import com.vls.reggie.service.SetmealDishService;
import com.vls.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.addWithSetmealDish(setmealDto);
        return R.success("新增套餐成功！");
    }


    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name){
        Page<Setmeal> setmealPageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPageInfo = new Page<>();

        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name!=null, Setmeal::getName, name);
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPageInfo, lambdaQueryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(setmealPageInfo, setmealDtoPageInfo, "records");

        List<Setmeal> setmealList = setmealPageInfo.getRecords();
        List<SetmealDto> setmealDtos = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //copy除了categoryName之外的属性
            BeanUtils.copyProperties(item, setmealDto);
            //查询categoryName
            Category category = categoryService.getById(item.getCategoryId());
            setmealDto.setCategoryName(category.getName());
            return setmealDto;

        }).collect(Collectors.toList());

        setmealDtoPageInfo.setRecords(setmealDtos);

        return R.success(setmealDtoPageInfo);
    }


    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)//删除setmealCache分类下的全部缓存
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithSetmealDish(ids);

        return R.success("套餐数据删除成功！");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDishes(id);

        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDishes(setmealDto);
        return R.success("修改成功！");
    }


    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids){
        setmealService.updateStatusByIds(status, ids);
        return R.success("修改成功");
    }

    /**
     * 返回套餐列表，用户端展示
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(setmeal.getCategoryId() != null,  Setmeal::getCategoryId, setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmeals = setmealService.list(setmealLambdaQueryWrapper);

        return R.success(setmeals);

    }

}
