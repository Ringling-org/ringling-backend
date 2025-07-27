package org.ringling.backend.user.controller;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.PathVariable;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.common.dto.ApiResponse;
import org.ringling.backend.user.dto.UserInfo;
import org.ringling.backend.user.service.UserService;

@Slf4j
@RequestMapping("/api/user")
@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse<?> getUserInfo(@PathVariable("id") Integer id) {
        UserInfo userInfo = userService.getUserInfo(id);

        return ApiResponse.success(userInfo);
    }
}
