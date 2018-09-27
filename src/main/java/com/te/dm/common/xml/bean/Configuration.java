package com.te.dm.common.xml.bean;


import com.te.dm.bean.Service;

import java.util.List;

/**
 * @author DM
 */

public class Configuration {
    private String version;
    private String name;
    private String description;

    public String getVersion() {
        return version;
    }

    private List<Service> services;

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Service> getServices() {
        return services;
    }
}
