package com.te.dm.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author DM
 */
public class ShellUtil {
    private static final Logger logger = LogManager.getLogger(ShellUtil.class);
    private DefaultExecutor defaultExecutor;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PumpStreamHandler pumpStreamHandler;

    public ShellUtil() {
        defaultExecutor = new DefaultExecutor();
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
        defaultExecutor.setStreamHandler(pumpStreamHandler);
    }

    /**
     * 调用命令
     *
     * @param command
     * @param strs
     * @return
     */
    public ShellUtil execute(String command, String... strs) {
        try {
            CommandLine cmdLine = new CommandLine(command);
            for (String val : strs) {
                cmdLine.addArgument(val);
            }
            defaultExecutor.execute(cmdLine);
        } catch (IOException e) {
            logger.error("IOException>", e);
        }

        return this;
    }

    /**
     * 输出标准信息
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getOutAsString() throws UnsupportedEncodingException {
        return outputStream.toString("GBK");
    }

    /**
     * 输出错误信息
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getErrorAsString() throws UnsupportedEncodingException {
        return errorStream.toString("GBK");
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        ShellUtil su = new ShellUtil();
        su.execute("netstat", "-a", "-n", "-o");

        System.out.println(su.getOutAsString());
    }
}
