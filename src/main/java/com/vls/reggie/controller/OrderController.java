package com.vls.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vls.reggie.common.R;
import com.vls.reggie.dto.OrdersDto;
import com.vls.reggie.entity.Orders;
import com.vls.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }


    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String number,String beginTime,String endTime){
        Page<OrdersDto> ordersDtoPage = orderService.pageWithAddressAndUserInfo(page, pageSize, number, beginTime, endTime);
        return R.success(ordersDtoPage);
    }

    /**
     * 用户端，个人中心展示
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> getLastPage(Integer page, Integer pageSize){
        Page<OrdersDto> ordersDtoPage = orderService.pageWithOrdersDetails(page, pageSize);
        return R.success(ordersDtoPage);
    }

    /**
     * 后台，订单派送
     */
    @PutMapping
    public R<String> delivery(@RequestBody Orders orders){
        log.info("order: {}", orders);
        Long id = orders.getId();
        Integer status = orders.getStatus();
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getId, id);
        Orders one = orderService.getOne(lambdaQueryWrapper);
        one.setStatus(status);
        orderService.updateById(one);
        return R.success("成功派送");
    }
}