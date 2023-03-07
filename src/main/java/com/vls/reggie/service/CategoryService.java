package com.vls.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vls.reggie.entity.Category;

public interface CategoryService extends IService<Category> {

    void remove(Long ids);

}
