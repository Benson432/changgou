package com.changgou.file.util;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @description: FastDFS客户端
 * @author: Benson
 * @time: 2021/6/29 23:12
 */
public class FastDFSClient {

    /*
      @description: 初始化Tracker
     */
    static {
        try {
            //获取tracker的配置文件fdfs_client.conf的位置
            String filePath = new ClassPathResource("fdfs_client.conf").getPath();
            //加载tracker配置信息
            ClientGlobal.init(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description: 获取TrackerServer
     * @return: org.csource.fastdfs.TrackerServer
     * @author: Benson
     * @time: 2021/7/1 23:19
     */
    public static TrackerServer getTrackerServer() throws Exception {
        //创建Tracker客户端对象 TrackerClient
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient 访问TrackerServer，获取连接信息
        return trackerClient.getConnection();
    }

    /**
     * @description: 获取StorageClient
     * @param trackerServer TrackerServer
     * @return: org.csource.fastdfs.StorageClient
     * @author: Benson
     * @time: 2021/7/1 23:21
     */
    public static StorageClient getStorageClient(TrackerServer trackerServer) {
        //通过连接信息创建StorageClient
        return new StorageClient(trackerServer, null);
    }


    /**
     * @description: 上传文件
     * @param fastDFSFile 文件
     * @return: java.lang.String[]
     * @author: Benson
     * @time: 2021/6/30 23:02
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception
    {
        NameValuePair[] nameValuePairs = new NameValuePair[1];
        nameValuePairs[0] = new NameValuePair("author", fastDFSFile.getAuthor());

        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = getStorageClient(trackerServer);

        //通过StorageClient上传文件 返回文件地址
        return storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), nameValuePairs);
    }


    /**
     * @description: 获取文件信息
     * @param groupName 组名
     * @param remoteFileName 文件名
     * @return: org.csource.fastdfs.FileInfo
     * @author: Benson
     * @time: 2021/6/30 23:09
     */
    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception {
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = getStorageClient(trackerServer);

        //获取文件信息
         return storageClient.get_file_info(groupName, remoteFileName);
    }


    /**
     * @description:
     * @param groupName 组名
     * @param remoteFileName 文件名
     * @return: java.io.InputStream
     * @author: Benson
     * @time: 2021/6/30 23:14
     */
    public static InputStream download(String groupName, String remoteFileName) throws Exception {
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = getStorageClient(trackerServer);

        //下载文件
        byte[] bytes = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * @description: 删除文件
     * @param groupName 组名
     * @param remoteFileName 文件名
     * @return: void
     * @author: Benson
     * @time: 2021/6/30 23:38
     */
    public static void deleteFile(String groupName, String remoteFileName) throws Exception{
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = getStorageClient(trackerServer);

        //删除文件
        storageClient.delete_file(groupName, remoteFileName);
    }

    /**
     * @description: 获取Storage信息
     * @return: org.csource.fastdfs.StorageServer
     * @author: Benson
     * @time: 2021/6/30 23:47
     */
    public static StorageServer getStorage() throws Exception {
        //创建Tracker客户端对象 TrackerClient
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient 访问TrackerServer，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        return trackerClient.getStoreStorage(trackerServer);
    }

    /**
     * @description: 获取Storage IP和端口信息
     * @param groupName 组名
     * @param remoteFileName 文件名
     * @return: org.csource.fastdfs.ServerInfo[]
     * @author: Benson
     * @time: 2021/6/30 23:51
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws Exception {
        //创建Tracker客户端对象 TrackerClient
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient 访问TrackerServer，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }

    /**
     * @description: 获取Tracker信息
     * @return: java.lang.String
     * @author: Benson
     * @time: 2021/7/1 23:26
     */
    public static String getTrackerInfo() throws Exception {
        TrackerServer trackerServer = getTrackerServer();

        String ip = trackerServer.getInetSocketAddress().getHostString();
        int tracker_http_port = ClientGlobal.getG_tracker_http_port();
        return "http://" + ip + ":" + tracker_http_port;

    }

}
