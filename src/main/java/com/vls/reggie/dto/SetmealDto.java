package com.vls.reggie.dto;

import com.vls.reggie.entity.Setmeal;
import com.vls.reggie.entity.SetmealDish;
import com.vls.reggie.entity.Setmeal;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
