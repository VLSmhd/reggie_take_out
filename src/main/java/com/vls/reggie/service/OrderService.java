package com.vls.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vls.reggie.dto.OrdersDto;
import com.vls.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    /**
     * 订单详情，后台
     * @param page
     * @param pageSize
     * @return
     */
    Page<OrdersDto> pageWithAddressAndUserInfo(Integer page, Integer pageSize, String number, String beginTime, String endTime);

    Page<OrdersDto> pageWithOrdersDetails(Integer page, Integer pageSize);
}
