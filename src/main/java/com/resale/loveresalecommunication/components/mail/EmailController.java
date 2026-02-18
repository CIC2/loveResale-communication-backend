package com.resale.loveresalecommunication.components.mail;

import com.resale.loveresalecommunication.components.mail.dto.UserOtpMailDTO;
import com.resale.loveresalecommunication.components.sms.dto.MessageRequestDTO;
import com.resale.loveresalecommunication.logging.LogActivity;
import com.resale.loveresalecommunication.models.enums.ActionType;
import com.resale.loveresalecommunication.security.CheckPermission;
import com.resale.loveresalecommunication.security.CookieBearerTokenResolver;
import com.resale.loveresalecommunication.security.JwtTokenUtil;
import com.resale.loveresalecommunication.utils.ReturnObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final CookieBearerTokenResolver tokenResolver;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/user/sendOtpMail")
    @LogActivity(ActionType.SEND_OTP_MAIL)
//@CheckPermission(value = {"admin:login", "sales:login"}, match = MatchType.ANY)
    public ResponseEntity<?> sendOtpMail(
            @RequestBody UserOtpMailDTO userOtpMailDTO) {

        return ResponseEntity.ok(
                emailService.sendUserOtpMail(userOtpMailDTO)
        );
    }
    @PostMapping("/user/sendMail")
    @LogActivity(ActionType.SNED_MAIL)
    public ResponseEntity<?> sendMail(
            @RequestBody UserOtpMailDTO userOtpMailDTO) {

        return ResponseEntity.ok(
                emailService.sendUserMail(userOtpMailDTO)
        );
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CheckPermission(value = {"admin:login"})
    @LogActivity(ActionType.UPLOAD_EMAIL)
    public ResponseEntity<?> uploadEmail(
            HttpServletRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "messageContent") String messageContent,
            @RequestParam(value = "sendToAll", required = false, defaultValue = "false") boolean sendToAll
    ) {
        String token = tokenResolver.resolve(request);

        Integer userId = null;
        if (token != null) {
            userId = jwtTokenUtil.extractUserId(token);
        }

        MessageRequestDTO data = new MessageRequestDTO();
        data.setTitle(title);
        data.setMessageContent(messageContent);
        data.setSendToAll(sendToAll);

        return emailService.sendEmailFromExcelSheet(file, data, userId);
    }

    @GetMapping
    @LogActivity(ActionType.GET_EMAILS)
    public ResponseEntity<?> getAllMails(
            @RequestParam(value = "search",required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ReturnObject returnObject = emailService.findAllMails(search,PageRequest.of(page, size));
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

