package com.nio.common;

/**
 * 公共信息
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月18日 上午10:51:52
 */
public class Commom {
    
    /**
     * Time server 的查询命令
     */
    public static final String COMMAND = "QUERY TIME ORDER";
    
    /**
     * Time server 的绑定端口
     */
    public static final int PORT = 8888;
    
    /**
     * Time server 的查询命令的长度,用于FixedLengthFrameDecoder进行黏包解包处理
     */
    public static final int  CAMMAND_BYTE_LENGTH = Commom.COMMAND.getBytes().length;
    
    /**
     * 分隔符,用于DelimiterBasedFrameDecoder进行黏包解包处理
     */
    public static final String DELIMITER = "###";
}
