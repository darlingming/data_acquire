package com.te.dm.service;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;

public class Exec {
    /**
     * 执行不需要返回结果的命令
     *
     * @throws Exception
     */
    public void execCmdWithoutResult() throws Exception {
        //开启windows telnet: net start telnet
        //注意：第一个空格之后的所有参数都为参数
        CommandLine cmdLine = new CommandLine("net");
        cmdLine.addArgument("start");
        cmdLine.addArgument("telnet");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(1);
        //设置60秒超时，执行超过60秒后会直接终止
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
        executor.setWatchdog(watchdog);
        DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
        executor.execute(cmdLine, handler);
        //命令执行返回前一直阻塞
        handler.waitFor();
    }

    /**
     * 带返回结果的命令执行
     *
     * @return
     */
    private String execCmdWithResult() {
        try {
            String command = "ping 192.168.1.10";
            //接收正常结果流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //接收异常结果流
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(command);
            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(null);
            //设置一分钟超时
            ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
            exec.setWatchdog(watchdog);
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
            exec.setStreamHandler(streamHandler);
            exec.execute(commandline);
            //不同操作系统注意编码，否则结果乱码
            String out = outputStream.toString("GBK");
            String error = errorStream.toString("GBK");
            return out + error;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public void test3() {
        try {
            Configurations configs = new Configurations();
            // setDefaultEncoding是个静态方法,用于设置指定类型(class)所有对象的编码方式。
            // 本例中是PropertiesConfiguration,要在PropertiesConfiguration实例创建之前调用。
            FileBasedConfigurationBuilder.setDefaultEncoding(PropertiesConfiguration.class, "UTF-8");
            PropertiesConfiguration propConfig = configs.properties(this.getClass().getClassLoader().getResource("log4j.properties"));

            System.out.println(propConfig.getString("log4j.appender.CONSOLE.Target"));
            System.out.println(propConfig.getBoolean("log4j.appender.LOGFILE.Append"));
            System.out.println(propConfig.getString("test"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
