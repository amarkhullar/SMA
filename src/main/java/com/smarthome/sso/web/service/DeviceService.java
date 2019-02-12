package com.smarthome.sso.web.service;



import com.smarthome.sso.web.domain.Device;
import com.smarthome.sso.web.domain.DeviceRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    @Autowired
    private DeviceRespository dRepo;

    public void addOneDevice(Device d){
        dRepo.save(d);
    }
    public void deleteDevice(Device d){
        dRepo.delete(d);
    }

    public List<Device> findAllDevices() { return dRepo.findAll();}

    public void deleteSelf() {dRepo.deleteAll();}

}