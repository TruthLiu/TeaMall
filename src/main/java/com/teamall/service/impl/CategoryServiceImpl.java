package com.teamall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.teamall.common.Const;
import com.teamall.common.ResponseCode;
import com.teamall.common.ServerResponse;
import com.teamall.dao.CategoryMapper;
import com.teamall.pojo.Category;
import com.teamall.pojo.User;
import com.teamall.service.ICategoryService;
import com.teamall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger= LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    //添加品种
    public ServerResponse addCategory(String categoryName,Integer parentId){
        if (parentId==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加茶叶品类参数错误！");
        }

        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        //将类品添加
        int rowCount=categoryMapper.insert(category);
        if (rowCount>0){
            return ServerResponse.createBySuccess("添加茶叶品类成功！");
        }
        return ServerResponse.createByErrorMessage("添加茶叶品类失败！");
    }

    //更新品种名称
    public ServerResponse updateCategoryName(Integer categoryId,String categoryName){
        if (categoryId==null||StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更改茶叶品类参数错误！");
        }

        Category category=new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        category.setUpdateTime(new Date());


        int rowCount=categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount>0){
            return ServerResponse.createBySuccessMessage("更新茶叶品类名称成功！");
        }
        return ServerResponse.createByErrorMessage("更新茶叶品类名称失败！");
    }

    //获取的是一个集合
    //获取子节点同级品种
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }


    /**
     * 递归查询本节点和孩子节点的ｉｄ
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet= Sets.newHashSet();
        findChildCategory(categorySet,categoryId);

        List<Integer> categoryIdList= Lists.newArrayList();
        if (categoryId!=null){
            for (Category categoryItem:categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);

    }

    //递归算法，算出子节点，为上面selectCategoryAndChildrenById调用使用
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category=categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
            categorySet.add(category);
        }
        //查找子节点，递归算法一定要有一个退出条件
        List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem:categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }






}
