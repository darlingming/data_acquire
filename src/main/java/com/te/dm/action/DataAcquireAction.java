package com.te.dm.action;

import com.alibaba.fastjson.JSON;
import com.te.dm.bean.Service;
import com.te.dm.bean.Shell;
import com.te.dm.common.xml.XmlConfigurationFactory;
import com.te.dm.common.xml.bean.Configuration;
import com.te.dm.service.DAService;
import com.te.dm.utils.ShellUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * @author DM
 */
public class DataAcquireAction {
    private static final Logger logger = LogManager.getLogger(DataAcquireAction.class);

    /**
     * 程序入口
     *
     * @param args
     */
    public static void main(String[] args) {
        logger.info(Arrays.toString(args));

        try {
            String configFilePath = args[0];//配置文件路径

            configFilePath = "D:\\DearM\\idea_workspace\\data_acquire\\src\\main\\resources\\data_acquire.xml";
            Configuration config = XmlConfigurationFactory.loadFile(configFilePath, Configuration.class);
            logger.info(config.getName() + ":" + config.getVersion());

            logger.info(JSON.toJSONString(config));
            for (Service service : config.getServices()) {
                //调用服务
                DAService dAService = (DAService) Class.forName(service.getServiceClass()).newInstance();
                dAService.execute(service);

                Shell shell = service.getShell();
                //调用命令
                if (null != shell && shell.isVerification()) {
                    ShellUtil shellUtil = new ShellUtil().execute(shell.getCommand(), shell.getArguments());
                    String resOut = shellUtil.getOutAsString();
                    String resErr = shellUtil.getErrorAsString();
                    logger.info(resOut + "=" + resErr);
                }

            }

            logger.info("全部结束");
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            System.exit(0);
        }
    }
}
