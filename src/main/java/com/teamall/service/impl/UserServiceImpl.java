package com.teamall.service.impl;

import com.teamall.common.Const;
import com.teamall.common.ServerResponse;
import com.teamall.common.TokenCache;
import com.teamall.dao.UserMapper;
import com.teamall.pojo.User;
import com.teamall.service.IUserService;
import com.teamall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String usrname, String password) {
        int resultCount=userMapper.checkUsername(usrname);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在。。。。");
        }

        //　ｔｏｄｏ　密码登录ＭＤ５
        String md5Password=MD5Util.MD5EncodeUtf8(password);
        User user=userMapper.selectLogin(usrname,md5Password);
        if (user==null){
            return ServerResponse.createByErrorMessage("密码错误。。。");
        }

        user.setPasswd(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }


    //注册
    public ServerResponse<String> register(User user){
       /* int resultCount=userMapper.checkUsername(user.getUsername());
        if (resultCount>0){
            return ServerResponse.createByErrorMessage("用户名已经存在。。。");
        }*/

       /* resultCount=userMapper.checkEmail(user.getEmail());
        if (resultCount>0){
            return ServerResponse.createByErrorMessage("email已经存在。。。");
        }*/

        //校验用户名
        ServerResponse validResponse=this.checkValid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        //校验邮箱
        validResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        //校验电话号码
        validResponse=this.checkValid(user.getPhone(),Const.PHONE);
        if (!validResponse.isSuccess()){
            return validResponse;
        }


        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPasswd(MD5Util.MD5EncodeUtf8(user.getPasswd()));

        int resultCount=userMapper.insert(user);
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败。。。");
        }
        return ServerResponse.createBySuccessMessage("注册成功。。。");
    }


    //检查用户名，电话，邮箱是否有效，即是否没被使用
    public ServerResponse<String> checkValid(String str,String type){
        if (StringUtils.isNotBlank(type)){
            //开始校验
            if (Const.USERNAME.equals(type)){
                int resultCount=userMapper.checkUsername(str);
                if (resultCount>0){
                    return ServerResponse.createByErrorMessage("用户名已存在。。。");
                }
            }

            if (Const.EMAIL.equals(type)){
                int resultCount=userMapper.checkEmail(str);
                if (resultCount>0){
                    return ServerResponse.createByErrorMessage("邮箱已经存在。。。");
                }
            }

            if (Const.PHONE.equals(type)){
                int resultCount=userMapper.checkPhone(str);
                if (resultCount>0){
                    return ServerResponse.createByErrorMessage("电话号码已经被使用。。。");
                }
            }
        }else {

            return ServerResponse.createByErrorMessage("参数错误。。。");
        }
        return ServerResponse.createBySuccessMessage("检验成功。。。");
    }


    //得到用户设置的问题的信息
    public ServerResponse selectQuestion(String username){
        ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String question=userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }


    //检查答案与问题是否匹配，是否正确
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount=userMapper.checkAnswer(username,question,answer);
        if (resultCount>0){
            //说明问题及问题的答案是这个用户的，并且是正确的
            String forgetToken= UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }

        return ServerResponse.createByErrorMessage("问题的答案错误");
    }


    //忘记密码，重新设置密码
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token需要传递。");
        }

        ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期。。");
        }

        if (StringUtils.equals(forgetToken,token)){
            //修改密码的操作
            String md5Password=MD5Util.MD5EncodeUtf8(passwordNew);
            int rowcount=userMapper.updatePasswordByUsername(username,md5Password);

            if (rowcount>0){
                return ServerResponse.createBySuccessMessage("修改密码成功！");
            }
        }else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }


    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权，要校验一下这个用户的旧密码，一定要指定是这个用户
        //因为我们会查询一个count,如果不指定id,那么结果就是true，即count>0
        int resultCount=userMapper.checkPassword(passwordOld,user.getId());
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("旧密码错误！");
        }

        user.setPasswd(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount=userMapper.updateByPrimaryKeySelective(user);
        if (updateCount>0){
            return ServerResponse.createBySuccessMessage("密码更新成功！");
        }
        return ServerResponse.createByErrorMessage("密码更新失败！");

    }


    public ServerResponse<User> update_information(User user){
        //username是不能被更新的
        //email也要进行一个校验，校验新的email是不是已经存在，
        // 并且存在的email如果相同的话,不能是我们当前的这个用户的。
        int resultCount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount>0){
            return ServerResponse.createByErrorMessage("邮箱已经存在，请更新它！");
        }
        User updateUser= new User();
        updateUser.setId(user.getId());
        updateUser.setPasswd(user.getPasswd());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());

        int updateCount=userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount>0){
            return ServerResponse.createBySuccess("更新个人信息成功!",updateUser);
        }

        return ServerResponse.createByErrorMessage("更新个人信息失败！");
    }


    public ServerResponse<User> getInformation(Integer userId){
        User user=userMapper.selectByPrimaryKey(userId);
        if (user==null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPasswd(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


    //backend
    //校验是否是管理员
    public ServerResponse checkAdminRole(User user){
        if (user!=null && user.getRole().intValue()==Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
