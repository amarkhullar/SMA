package com.smarthome.sso.web.service;



import com.smarthome.sso.web.constants.ServiceResult;
import com.smarthome.sso.web.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository tRepo;

    @Autowired
    private Task2Repository task2Repository;

    public void addOneTask(Task t){
        tRepo.save(t);
    }

    public void deleteTask(Task t){
        tRepo.delete(t);
    }

    public void deleteTask2(Task2 task2){
        task2Repository.delete(task2);
    }

    public void deleteTasks(List<Task> tasks){
        tRepo.deleteAll(tasks);
    }

    public Task findTaskByTaskId(String taskId){
        return tRepo.findByTaskId(taskId);
    }

    public Task2 findTask2ByTaskId(String taskId){
        return task2Repository.findByTaskId(taskId);
    }

    public List<Task> findTasksByDeviceId(String deviceId){
        return tRepo.findByDeviceId(deviceId);
    }

    public List<Task2> findTask2sByDeviceId(String deviceId){
        return task2Repository.findTask2sByDeviceId(deviceId);
    }

    public ServiceResult deleteOneTaskByTaskId(String id){
        //FIX - DOES NOT HAVE THE VERIFICAITON USER SERVICE HAS
        tRepo.deleteByTaskId(id);
        return ServiceResult.SERVICE_SUCCESS;
    }

    public List<Task> findAllTasks() {
        return tRepo.findAll();
    }

    public List<Task> findAllTasksSortedByTime(){
        return tRepo.findAllByOrderByCalendarAsc();
    }

    public void deleteSelf() {tRepo.deleteAll();}

    public void addTask2(Task2 task2){
        task2Repository.save(task2);
    }

    public List<Task2> findAllTask2s(){
        return task2Repository.findAll();
    }

}