package com.vls.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vls.reggie.common.BaseContext;
import com.vls.reggie.common.R;
import com.vls.reggie.entity.ShoppingCart;
import com.vls.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        //根据userId查询
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        lambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        return R.success(shoppingCartService.list(lambdaQueryWrapper));
    }

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //设置用户id，指定是哪个用户的购物车
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //假如连续插入两条同样的菜品，不能都添加进去，只需设置数量多少即可
        //查询数据库是否含有该菜品|套餐
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(shoppingCart.getUserId() != null, ShoppingCart::getUserId, userId);
        if(dishId != null){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);

        }else {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(lambdaQueryWrapper);
        //如果有，就数量加1
        if(shoppingCart1 != null){
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            shoppingCartService.updateById(shoppingCart1);
        }else{
            //没有就添加
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            shoppingCart1 = shoppingCart;
        }

        return R.success(shoppingCart1);

    }


    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(lambdaQueryWrapper);
        return R.success("购物车已清空");
    }

    /**
     * 对应菜品套餐减少数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        //通过用户id 和 dishid/ setmealId查购物车
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq( ShoppingCart::getUserId, BaseContext.getCurrentId());
        //查询是套餐还是菜品
        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        }else{
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(lambdaQueryWrapper);
        if(one.getNumber() <= 1){
            shoppingCartService.remove(lambdaQueryWrapper);
            shoppingCart.setNumber(0);
            return R.success(shoppingCart);
        }else{
            one.setNumber(one.getNumber() - 1);
            shoppingCartService.updateById(one);
        }

        return R.success(one);

    }
}