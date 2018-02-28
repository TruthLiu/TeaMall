package com.teamall.service.impl;

import com.google.common.collect.Lists;
import com.teamall.service.IFileService;
import com.teamall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService{

    private Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file,String path){
        String fileName=file.getOriginalFilename();
        //扩展名  例如　abc.jpg 下面获取jpg
        String fileExtentionName=fileName.substring(fileName.lastIndexOf(".")+1);
        //防止文件名重复
        String uploadFileName= UUID.randomUUID().toString()+"."+fileExtentionName;

        logger.info("开始上传文件，上传文件的文件名为：{},上传的路径：{}，新文件名：{}",fileName,path,uploadFileName);

        File fileDir=new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile=new File(path,uploadFileName);

        try {
            file.transferTo(targetFile);
            //文件已经上传成功了

            //将targetFile上传到我们的FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            //上传完之后，删除upload下面的文件
            targetFile.delete();

        } catch (IOException e) {
            logger.info("上传文件异常",e);
            return null;
        }

        return targetFile.getName();


    }

//    public static void main(String[] args) {
//        String fileName="aklj',/.,nkn.jpg";
//        System.out.println(fileName.substring(fileName.lastIndexOf(".")+1));
//    }
}
