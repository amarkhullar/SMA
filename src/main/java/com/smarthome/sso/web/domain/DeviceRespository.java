package com.smarthome.sso.web.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DeviceRespository extends MongoRepository<Device, String> {

    Device findByUserId(String id);

    void delete(Device d);

    @Query(value = "{'userId':?0}", count = true)
    Integer getDeviceCountFromUserId(String userId);

}
