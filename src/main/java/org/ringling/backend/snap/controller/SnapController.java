package org.ringling.backend.snap.controller;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.bind.annotation.RequestParam;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.common.dto.ApiResponse;
import org.ringling.backend.config.JwtAuth;
import org.ringling.backend.snap.dto.SnapResponse;
import org.ringling.backend.snap.service.SnapService;
import org.ringling.backend.user.entity.User;

@Slf4j
@RequestMapping("/api/snap")
@Controller
public class SnapController {

    private final SnapService snapService;

    @Autowired
    public SnapController(SnapService snapService) {
        this.snapService = snapService;
    }

    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public SnapResponse createSnap(@JwtAuth User user, @RequestParam("url") String url) {
        Integer userId = user == null ? null : user.getId();
        return snapService.processSnap(userId, url);
    }

    @RequestMapping(value = "/guest", method = {RequestMethod.POST})
    @ResponseBody
    public SnapResponse createSnapForGuest(@RequestParam("url") String url) {
        return snapService.processSnapForGuest(url);
    }

    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public ApiResponse<List<SnapResponse>> getAllSnaps() {
        return ApiResponse.success(snapService.getAllSnaps());
    }
}
