package org.ringling.backend.snap.controller;

import static org.ringling.backend.common.code.ErrorCode.LOGIN_REQUIRED;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.http.HttpStatus;
import froggy.winterframework.http.ResponseEntity;
import froggy.winterframework.stereotype.Controller;
import froggy.winterframework.web.bind.annotation.RequestBody;
import froggy.winterframework.web.bind.annotation.RequestMapping;
import froggy.winterframework.web.bind.annotation.RequestMethod;
import froggy.winterframework.web.bind.annotation.RequestParam;
import froggy.winterframework.web.bind.annotation.ResponseBody;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.ringling.backend.auth.exception.AuthException;
import org.ringling.backend.common.controller.BaseApiController;
import org.ringling.backend.config.JwtAuth;
import org.ringling.backend.snap.dto.CreateSnapRequest;
import org.ringling.backend.snap.dto.SnapCountResponse;
import org.ringling.backend.snap.dto.SnapRequestUrl;
import org.ringling.backend.snap.dto.SnapResponse;
import org.ringling.backend.snap.service.SnapService;
import org.ringling.backend.user.entity.User;

@Slf4j
@RequestMapping("/api/snap")
@Controller
public class SnapController extends BaseApiController {

    private final SnapService snapService;

    @Autowired
    public SnapController(SnapService snapService) {
        this.snapService = snapService;
    }

    @RequestMapping(method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<SnapResponse> createSnap(
        @JwtAuth User user,
        @Valid @RequestBody CreateSnapRequest request
    ) {
        SnapRequestUrl url = new SnapRequestUrl(request.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(snapService.processSnap(user.getId(), url));
    }

    @RequestMapping(value = "/guest", method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<SnapResponse> createSnapForGuest(@Valid @RequestBody CreateSnapRequest request) {
        SnapRequestUrl url = new SnapRequestUrl(request.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(snapService.processSnapForGuest(url));
    }

    @RequestMapping(method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<List<SnapResponse>> getSnaps(
        @JwtAuth(required = false) User user,
        @RequestParam(value = "scope", defaultValue = "all") String scope,
        @RequestParam(value = "cursor", required = false) Integer lastSnapId,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        Integer searchUserId = null;

        if ("my".equals(scope)) {
            if (user == null) {
                throw new AuthException(LOGIN_REQUIRED);
            }
            searchUserId = user.getId();
        }

        return ResponseEntity.ok(snapService.getSnaps(searchUserId, lastSnapId, limit));
    }

    @RequestMapping(value = "/counts", method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<SnapCountResponse> getSnapCounts(@JwtAuth(required = false) User user) {
        Integer userId = (user != null) ? user.getId() : null;
        return ResponseEntity.ok(snapService.getSnapCounts(userId));
    }
}
