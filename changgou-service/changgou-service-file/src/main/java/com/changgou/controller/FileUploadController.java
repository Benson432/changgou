package com.changgou.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSClient;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description:
 * @author: Benson
 * @time: 2021/6/30 16:06
 */

@RestController
@CrossOrigin
@RequestMapping("/upload")
public class FileUploadController {

    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile file) throws Exception {
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),
                file.getBytes(),
                StringUtils.getFilenameExtension(file.getOriginalFilename())
        );

        String[] uploads = FastDFSClient.upload(fastDFSFile);

        String fileURL = FastDFSClient.getTrackerInfo() + "/" + uploads[0] + "/" + uploads[1];

        return new Result(true, StatusCode.OK, "SUCCESS", fileURL);
    }
}
