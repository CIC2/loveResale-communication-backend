package com.resale.homeflycommunication.components.sms;

import com.resale.homeflycommunication.components.sms.dto.MessageRequestDTO;
import com.resale.homeflycommunication.components.sms.dto.SingleSMSRequestDTO;
import com.resale.homeflycommunication.logging.LogActivity;
import com.resale.homeflycommunication.models.enums.ActionType;
import com.resale.homeflycommunication.security.CheckPermission;
import com.resale.homeflycommunication.security.CookieBearerTokenResolver;
import com.resale.homeflycommunication.security.JwtTokenUtil;
import com.resale.homeflycommunication.utils.ReturnObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    @Autowired
    SmsService smsService;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    CookieBearerTokenResolver tokenResolver;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CheckPermission(value = {"admin:login"})
    @LogActivity(ActionType.UPLOAD_SMS_EXCEL)
    public ResponseEntity<?> uploadExcel(
            HttpServletRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "messageContent") String messageContent,
            @RequestParam(value = "sendToAll", required = false, defaultValue = "false") boolean sendToAll

    ){
        String token = tokenResolver.resolve(request);

        Integer userId = null;
        if (token != null) {
            userId = jwtTokenUtil.extractUserId(token);
        }

        MessageRequestDTO data = new MessageRequestDTO();
        data.setTitle(title);
        data.setMessageContent(messageContent);
        data.setSendToAll(sendToAll);
        return smsService.sendSmsFromExcelSheet(file,data, userId);
    }

    @PostMapping(value = "/singleSMS")
    @LogActivity(ActionType.SEND_SINGLE_SMS)
    public ResponseEntity<?> sendSingleSms(
            @RequestBody SingleSMSRequestDTO singleSMSRequestDTO

    ){
        ReturnObject returnObject = new ReturnObject();

        if(smsService.sendSms(singleSMSRequestDTO.getMobile(), singleSMSRequestDTO.getContent())){
            returnObject.setData(true);
            returnObject.setStatus(true);
            returnObject.setMessage("Sent Successfully");
            return ResponseEntity.status(HttpStatus.OK).body(returnObject);
        }else{
            returnObject.setData(false);
            returnObject.setStatus(false);
            returnObject.setMessage("Failed to send OTP");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

    }

    @GetMapping
    @LogActivity(ActionType.GET_ALL_SMS)
    public ResponseEntity<?> getAllSMS(
            @RequestParam(value = "search",required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ReturnObject returnObject = smsService.findAllSMS(search, PageRequest.of(page, size));
        if(returnObject.getStatus()) {
            return ResponseEntity.ok(
                    returnObject
            );
        }else{
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        return ResponseEntity.ok(
                returnObject
        );
    }


}


