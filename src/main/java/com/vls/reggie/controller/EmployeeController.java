package com.vls.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vls.reggie.common.R;
import com.vls.reggie.entity.Employee;
import com.vls.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     * @param employee
     * @param request
     * @return
     */
    @PostMapping("/login")//post请求体方式，数据以JSON形式传入后台   HttpServletRequest是想把用户信息存到session
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        //将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        //根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee employee1 = employeeService.getOne(lambdaQueryWrapper);//数据库字段是唯一性约束
        //如果没有查询到则返回登录失败结果
        if(employee1 == null){
            return R.error("用户名不存在！");
        }
        //密码比对，如果不一致则返回登录失败结果
        if(!employee1.getPassword().equals(password)){
            return R.error("密码错误！");
        }
        //查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(employee1.getStatus() == 0){
            return R.error("账号已禁用权限！");
        }
        //登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", employee1.getId());
        return R.success(employee1);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @param request
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee, HttpServletRequest request){
        log.info("新增员工，员工信息：{}",employee.toString());
        //空值处理
        //统一给初始密码123456,md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
//        //时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //获得当前登录用户id
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        //调用service
        employeeService.save(employee);

        return R.success("添加成功");
    }


    /**
     * 获取员工列表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(Integer page, Integer pageSize, String name){//形参根据请求URL写
        //分页构造器
        Page pageInfo = new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo, lambdaQueryWrapper);
        return R.success(pageInfo);

    }


    /**
     * 修改员工信息
     * @param employee
     * @param request
     * @return
     */
    //如果执行修改操作, 后面的修改请求会把前面的请求给覆盖掉, 所以使用@PutMapping
    @PutMapping
    public R<String> update(@RequestBody Employee employee, HttpServletRequest request){
        log.info("修改的employee： {}", employee);

//       Long updaterId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(updaterId);
//        employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);
        return R.success("修改成功");

    }

    /**
     * 根据员工查询信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有对应员工信息");
    }


}
