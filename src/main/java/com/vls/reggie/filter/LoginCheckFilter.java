package com.vls.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.vls.reggie.common.BaseContext;
import com.vls.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录
 */

@WebFilter(filterName = "loginCheckFilter" ,urlPatterns = "/*")
@Slf4j
public  class LoginCheckFilter implements Filter{

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

//        1、获取本次请求的URI
        String requestURI = request.getRequestURI();

//        2、判断本次请求是否需要处理
        //不需要处理的请求
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/common/**",
                //静态资源
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"

        };
        boolean check = check(requestURI, urls);
//        3、如果不需要处理，则直接放行
        if(check){
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        log.info("拦截到请求: {}" , request.getRequestURI());//{}是一个占位符，省略+号，后面跟个参数，运行的时候参数会输出到大括号里。
//        4-1、判断管理员登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }

        // 4-2、判断用户登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("user"));
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }
//        5、如果未登录则返回未登录结果，通过输出流，响应数据
        log.info("用户未登录！");
        //根据request.js里的响应拦截器来书写逻辑
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    @Override
    public void destroy() {

    }


    //路径匹配,check为true不需要处理，否则拦截
    public boolean check(String requestURI, String[] urls){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }


}
