package com.teamall.controller.backend;


import com.teamall.common.Const;
import com.teamall.common.ServerResponse;
import com.teamall.pojo.User;
import com.teamall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/user")
//后台管理员
public class UserManagerController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    public ServerResponse<User> login(String username, String password, HttpSession session){

        ServerResponse<User> response=iUserService.login(username,password);
        if (response.isSuccess()){
            User user=response.getData();
            if (user.getRole()== Const.Role.ROLE_ADMIN){
                //说明登录的是管理员
                session.setAttribute(Const.CURRENT_USER,user);
                return response;
            }else {
                return ServerResponse.createByErrorMessage("不是管理员，无法登录！");
            }
        }
        //登录不成功，直接返回ｒｅｓｐｏｎｓｅ
        return response;
    }

}
