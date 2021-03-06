package com.smarthome.sso.web.controller;

import com.smarthome.sso.web.constants.ServiceResult;
import com.smarthome.sso.web.domain.*;
import com.smarthome.sso.web.service.DeviceService;
import com.smarthome.sso.web.service.FiwareService;
import com.smarthome.sso.web.service.TaskService;
import com.smarthome.sso.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class FiwareController {

    private Boolean task2Enable = true;

    @Autowired
    private FiwareService fiwareService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private UserService userService;

    public boolean innerMatchUserDevice(String inputToken, String deviceId){
        String username = userService.getUsernameFromSessionId(inputToken);
        User foundUser = userService.findOneUserByUsername(username);
        Device target = deviceService.findDeviceByDeviceId(deviceId);

        if (!foundUser.getUserId().equals(target.getUserId())){
            return false;
        }
        return true;
    }

    public boolean innerMatchDeviceTask(String deviceId, String task2Id){
        Task2 task2 = taskService.findTask2ByTaskId(task2Id);
        if (task2 == null){return false;}
        if (!task2.getDeviceId().equals(deviceId)){
            return false;
        }
        return true;
    }

    //Adds a new device associated with the user
    @PostMapping("/device/add")
    @ResponseBody
    public ResponseEntity<?> addDevice(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String username = userService.getUsernameFromSessionId(inputToken);
        User foundUser = userService.findOneUserByUsername(username);
        String type = String.valueOf(request.getParameter("type"));

        Device newDevice = new Device(foundUser.getUserId(), type,"","","");
        deviceService.addOneDevice(newDevice);
        System.out.println("Added new device to user "+foundUser.getUserId()+" of type "+type);

        return ResponseEntity.ok("Added new device to user "+foundUser.getUserId()+" of type "+type);

    }

    @PostMapping("/device/get")
    @ResponseBody
    public ResponseEntity<?> getOneDevice(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String deviceId = String.valueOf(request.getParameter("device"));
        if (!innerMatchUserDevice(inputToken,deviceId)){
            return ResponseEntity.ok("not match");
        }
        Device device = deviceService.findDeviceByDeviceId(deviceId);
        return ResponseEntity.ok(device);
    }

    @PostMapping("/device/change")
    @ResponseBody
    public ResponseEntity<?> changeDeviceType(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String deviceId = String.valueOf(request.getParameter("device"));
        String newType = String.valueOf(request.getParameter("input"));
        if (!innerMatchUserDevice(inputToken,deviceId)){
            return ResponseEntity.ok("not match");
        }
        Device device = deviceService.findDeviceByDeviceId(deviceId);
        device.setType(newType);
        deviceService.addOneDevice(device);
        return ResponseEntity.ok("finished");
    }

    //Deletes all devices, used for debugging
    @PostMapping("/deleteAllDevices")
    @ResponseBody
    public ResponseEntity<?> restartDeviceDB(HttpServletRequest request, HttpServletResponse response) throws Exception{
        deviceService.deleteSelf();
        System.out.println("All devices have been deleted");
        return ResponseEntity.ok("good");
    }





    @PostMapping("/device/verify")
    @ResponseBody
    public ResponseEntity<?> verifyDevices(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String username = userService.getUsernameFromSessionId(inputToken);
        User foundUser = userService.findOneUserByUsername(username);

        String deviceId = String.valueOf(request.getParameter("device"));
        Device target = deviceService.findDeviceByDeviceId(deviceId);
        String type = String.valueOf(request.getParameter("type"));

        if(target==null){
            return ResponseEntity.badRequest().body("Device ID not found");
        }

        if (!foundUser.getUserId().equals(target.getUserId())){
            return ResponseEntity.badRequest().body("device does not belong to the user");
        }

        if(!type.equals(target.getType())){
            return ResponseEntity.badRequest().body("Device type mismatch");
        }

        return ResponseEntity.ok("Device exists");




    }


    //Returns list of all devices and their owners, requires admin permission
    @PostMapping("/device/all")
    @ResponseBody
    public ResponseEntity<?> showAllDevices(HttpServletRequest request, HttpServletResponse response) throws Exception{
        /*
        String admin = String.valueOf(request.getParameter("admin"));
        if(admin.equals("admin")) {
            List<Device> dList = deviceService.findAllDevices();
            String s = "";
            for (int i = 0; i < dList.size(); i++) {
                Device d = dList.get(i);
                s = s + d.getType() + " owned by " + d.getUserId() + " ,";
            }
            return ResponseEntity.ok(s);
        }else{
        */
            return ResponseEntity.badRequest().body("Cannot display all devices for privacy reasons.");
        //}
    }




    //Deletes a device and all tasks associated with it
    @PostMapping("/device/delete")
    @ResponseBody
    public ResponseEntity<?> deleteDevice(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String username = userService.getUsernameFromSessionId(inputToken);
        User foundUser = userService.findOneUserByUsername(username);

        String deviceId = String.valueOf(request.getParameter("device"));
        Device target = deviceService.findDeviceByDeviceId(deviceId);

        if (!foundUser.getUserId().equals(target.getUserId())){
            return ResponseEntity.ok("device does not belong to the user");
        }

        deviceService.deleteDevice(target);

        List<Task> taskList = taskService.findTasksByDeviceId(deviceId);
        taskService.deleteTasks(taskList);

        System.out.println("Device deleted");
        return ResponseEntity.ok("Device deleted");

    }

    public void innerDeleteDevices(String userSessionId){
        List<Device> devices = deviceService.findDevicesByUserId(userSessionId);
        for (Device d : devices){
            deviceService.deleteDevice(d);
            List<Task> taskList = taskService.findTasksByDeviceId(d.getDeviceId());
            taskService.deleteTasks(taskList);
        }
    }


    //Toggles on or off one device, must be owned by the user
    @PostMapping("/device/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleDevice(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String deviceId = String.valueOf(request.getParameter("device"));
        if (!innerMatchUserDevice(inputToken,deviceId)){
            return ResponseEntity.ok("device does not belong to the user");
        }

        ServiceResult result = deviceService.toggleDevice(deviceId);
        if (result == ServiceResult.SERVICE_SUCCESS){
            System.out.println(deviceId + " toggled");
        }

        return ResponseEntity.ok("Device toggled");

    }





    //Shows all devices owned by the user
    @PostMapping("/device/user/all")
    @ResponseBody
    public ResponseEntity<?> showUserDevices(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String username = userService.getUsernameFromSessionId(inputToken);
        User foundUser = userService.findOneUserByUsername(username);

        List<Device> deviceList = deviceService.findDevicesByUserId(foundUser.getUserId());
        if (deviceList == null){
            return ResponseEntity.ok("");
        }
        List<Device> result = new ArrayList<>();
        //String s = "";
        for(int i=0; i<deviceList.size();i++){
            if(deviceList.get(i).getUserId().equals(foundUser.getUserId())) {
                Device d = deviceList.get(i);
                result.add(d);
                //s = s + d.getType() + " deviceId " + d.getDeviceId() +" CurrentlyOn "+d.getPowerStatus() +" , ";
            }
        }
        //return ResponseEntity.ok(s);
        return ResponseEntity.ok(result);
    }




    @RequestMapping(value = "/fiware/info", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<?> getApiInfo(HttpServletRequest request, HttpServletResponse response) throws Exception{
        //String token = String.valueOf(request.getParameter("sessionId"));
        //User tmpUser = getUserFromSessionId(token);
        //TODO

        FiwareInfo fiwareInfo = fiwareService.fiwareApiRequest("http://137.222.204.81:1026/v2/entities","testLuft2019","/testLuft2019");
        return ResponseEntity.ok(fiwareInfo);
    }

    @PostMapping("/fiware/task/add")
    @ResponseBody
    public ResponseEntity<?> addTask2(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String inputType = String.valueOf(request.getParameter("type"));
        String deviceId = String.valueOf(request.getParameter("device"));
        String conditionString = String.valueOf(request.getParameter("condition"));
        if (!innerMatchUserDevice(inputToken,deviceId)){
            return ResponseEntity.ok("device does not belong to the user");
        }
        Task2 task2 = new Task2(inputType,deviceId,conditionString);
        taskService.addTask2(task2);
        return ResponseEntity.ok("task2 added");
    }

    @PostMapping("/fiware/task/view")
    @ResponseBody
    public ResponseEntity<?> viewTask2(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String deviceId = String.valueOf(request.getParameter("device"));
        if (!innerMatchUserDevice(inputToken,deviceId)){
            return ResponseEntity.ok("device does not belong to the user");
        }
        List<Task2> list = taskService.findTask2sByDeviceId(deviceId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/fiware/task/patch")
    @ResponseBody
    public ResponseEntity<?> changeTask2(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String deviceId = String.valueOf(request.getParameter("device"));
        String task2Id = String.valueOf(request.getParameter("taskId"));
        String newType = String.valueOf(request.getParameter("type"));
        String newSyntax = String.valueOf(request.getParameter("condition"));
        if (!innerMatchUserDevice(inputToken,deviceId)){
            return ResponseEntity.ok("device does not belong to the user");
        }
        if (!innerMatchDeviceTask(deviceId,task2Id)){
            return ResponseEntity.ok("task does not belong to the device");
        }
        Task2 task2 = taskService.findTask2ByTaskId(task2Id);
        task2.setType(newType);
        task2.setTrigger(newSyntax);
        taskService.addTask2(task2);
        return ResponseEntity.ok("finished");
    }

    @PostMapping("/fiware/task/delete")
    @ResponseBody
    public ResponseEntity<?> deleteTask2(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String inputToken = String.valueOf(request.getParameter("token"));
        String deviceId = String.valueOf(request.getParameter("device"));
        String task2Id = String.valueOf(request.getParameter("taskId"));
        if (!innerMatchUserDevice(inputToken,deviceId)){
            return ResponseEntity.ok("device does not belong to the user");
        }
        if (!innerMatchDeviceTask(deviceId,task2Id)){
            return ResponseEntity.ok("task does not belong to the device");
        }
        Task2 task2 = taskService.findTask2ByTaskId(task2Id);
        taskService.deleteTask2(task2);
        return ResponseEntity.ok("finished");
    }

    public void innerDisableTask2Scheduler(){
        task2Enable = false;
    }
    public void innerEnableTask2Scheduler(){
        task2Enable = true;
    }

    public void innerDeleteAllTask2OfDevice(String deviceId) throws Exception{
        List<Task2> tasks = taskService.findTask2sByDeviceId(deviceId);
        for (Task2 task : tasks){
            taskService.deleteTask2(task);
        }
    }

    @Scheduled(fixedRate= 15000)
    public void checkFiwareTasks() throws Exception{
        if (!task2Enable) return;
        List<Task2> taskList = taskService.findAllTask2s();
        HashMap<String,FiwareInfo> deviceHashMap = new HashMap<>();
        for (Task2 task2 : taskList){
            if (!deviceHashMap.containsKey(task2.getDeviceId())){
                Device device = deviceService.findDeviceByDeviceId(task2.getDeviceId());
                deviceHashMap.put(task2.getDeviceId(),fiwareService.fiwareApiRequest("http://137.222.204.81:1026/v2/entities","testLuft2019","/testLuft2019"));
            }
        }
        for (Task2 task2 : taskList){
            ServiceResult result = fiwareService.trySyntax(deviceHashMap.get(task2.getDeviceId()),task2.getTrigger());
            if (result == ServiceResult.SERVICE_SUCCESS){
                String taskType = task2.getType();
                if ("DefaultType".equals(taskType) || "Toggle".equals(taskType)){
                    deviceService.toggleDevice(task2.getDeviceId());
                }
                else if ("TurnOn".equals(taskType)){
                    deviceService.turnOnDevice(task2.getDeviceId());
                }
                else if ("TurnOff".equals(taskType)){
                    deviceService.turnOffDevice(task2.getDeviceId());
                }
            }
        }
        System.out.println("---Task2: Refreshed---");
    }

}
