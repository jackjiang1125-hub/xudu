package org.jeecg.modules.iot.device.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.config.shiro.IgnoreAuth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/event")
public class IotHKDeviceController {


    @IgnoreAuth
    @ResponseBody
    @RequestMapping(value = "/record", method = RequestMethod.POST, produces = "application/json")
    public Result<String> record(@RequestParam(value = "event_log", required = false) String event_log, @RequestParam(value = "Picture", required = false) MultipartFile Picture, HttpServletRequest request) {

        System.out.println(event_log);
        return Result.OK("success");
    }
}
