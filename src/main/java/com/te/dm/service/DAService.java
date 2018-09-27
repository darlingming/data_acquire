package com.te.dm.service;

import com.te.dm.bean.Service;

import java.io.IOException;

/**
 * @author DM
 */
public interface DAService {
    public int execute(Service service) throws IOException;

}
