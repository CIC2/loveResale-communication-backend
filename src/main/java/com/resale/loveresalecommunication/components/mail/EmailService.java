package com.resale.loveresalecommunication.components.mail;

import com.resale.loveresalecommunication.components.mail.dto.MailListResponseDTO;
import com.resale.loveresalecommunication.components.mail.dto.UserOtpMailDTO;
import com.resale.loveresalecommunication.components.notification.user.dto.NotificationListResponseDTO;
import com.resale.loveresalecommunication.components.sms.dto.MessageRequestDTO;
import com.resale.loveresalecommunication.feign.CustomerClient;
import com.resale.loveresalecommunication.models.Communication;
import com.resale.loveresalecommunication.repositories.CommunicationRepository;
import com.resale.loveresalecommunication.utils.ExcelSheetUtils;
import com.resale.loveresalecommunication.utils.PaginatedResponseDTO;
import com.resale.loveresalecommunication.utils.ReturnObject;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {


    private static int MAIL = 2;

    private final JavaMailSender javaMailSender;
    private final CustomerClient customerClient;
    private final ExcelSheetUtils excelSheetUtils;
    private final CommunicationRepository communicationRepository;

    public ResponseEntity<?> sendUserOtpMail(UserOtpMailDTO userOtpMailDTO) {
        String htmlMessage =
                "<div style='font-family: Arial, sans-serif; padding: 20px; background:#f7f7f7;'>" +
                        "<div style='max-width:500px; margin:auto; background:white; padding:20px; " +
                        "border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1);'>" +
                        "<h2 style='color:#1a73e8;'>Password Reset Verification</h2>" +
                        "<p>Hello,</p>" +
                        "<p>Your password reset OTP is:</p>" +
                        "<div style='font-size:28px; font-weight:bold; color:#1a73e8; margin:20px 0;'>"
                        + userOtpMailDTO.getOtp() +
                        "</div>" +
                        "<p>This OTP will expire in <b>3 minutes</b>.</p>" +
                        "<p>If you didn’t request this reset, please ignore this email.</p>" +
                        "<br><hr>" +
                        "<p style='font-size:12px; color:#777;'>© Talaat Moustafa Group</p>" +
                        "</div>" +
                        "</div>";

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Esales@talaatmoustafa.com");
            helper.setTo(userOtpMailDTO.getEmail());
            helper.setSubject(userOtpMailDTO.getMailSubject());
            helper.setText(htmlMessage, true); // true = send as HTML

            javaMailSender.send(message);
            return ResponseEntity.ok("OTP email sent successfully");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    public ReturnObject findAllMails(String search, Pageable pageable) {

        ReturnObject returnObject = new ReturnObject();
        List<MailListResponseDTO> mailListResponseDTO = new ArrayList<>();

        try {
            Pageable sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt") // change field name if needed
            );
            Page<Communication> communicationList =
                    communicationRepository.findCommunicationByType(MAIL, sortedPageable);

            // Normalize search text
            String searchText = (search == null) ? "" : search.trim().toLowerCase();

            List<Map<String, Object>> customers = null;
            try {
                ReturnObject response = customerClient.getCustomerEmails();
                if (response != null && response.getData() instanceof List) {
                    customers = (List<Map<String, Object>>) response.getData();
                }
            } catch (Exception feignException) {
                // 🔕 Ignore Feign failure completely
                System.out.println("Customer service unavailable, skipping customer names" + feignException);
                customers = null;
            }

            for (Communication communication : communicationList) {

                String mail = communication.getMail();

                // 🔍 SEARCH FILTER (contains)
                if (!searchText.isEmpty()
                        && (mail == null || !mail.toLowerCase().contains(searchText))) {
                    continue; // skip non-matching emails
                }

                MailListResponseDTO dto = new MailListResponseDTO();
                dto.setTitle(communication.getSubject());
                dto.setMessage(communication.getBody());
                dto.setCustomerMail(mail);
                dto.setSenderName(communication.getUserId().toString());
                dto.setSentAt(communication.getCreatedAt());

                // Find customer name
                if (customers != null) {
                    for (Map<String, Object> c : customers) {
                        String email = String.valueOf(c.get("email"));

                        if (mail != null && mail.equalsIgnoreCase(email)) {
                            dto.setCustomerName(String.valueOf(c.get("customerName")));
                            break;
                        }
                    }
                }

                mailListResponseDTO.add(dto);
            }


            PaginatedResponseDTO<MailListResponseDTO> paginatedResponse = new PaginatedResponseDTO<>(
                    mailListResponseDTO,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    communicationList.getTotalElements(),
                    communicationList.getTotalPages(),
                    communicationList.isLast()
            );


            returnObject.setData(paginatedResponse);
            returnObject.setMessage("Fetched Successfully");
            returnObject.setStatus(true);

        } catch (Exception exception) {
            returnObject.setData(null);
            returnObject.setMessage("Failed to Fetch Data");
            returnObject.setStatus(false);
        }

        return returnObject;
    }
    public ResponseEntity<?> sendUserMail(UserOtpMailDTO userOtpMailDTO) {

        String htmlMessage = userOtpMailDTO.getMailContent();

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Esales@talaatmoustafa.com");
            helper.setTo(userOtpMailDTO.getEmail());
            helper.setSubject(userOtpMailDTO.getMailSubject());
            helper.setText(htmlMessage, true); // true = HTML

            javaMailSender.send(message);

            return ResponseEntity.ok("Email sent successfully");

        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Failed to send email: " + e.getMessage());
        }
    }


    public ResponseEntity<?> sendEmailFromExcelSheet(MultipartFile file, MessageRequestDTO messageRequestDTO, Integer userId
    ) {

        ReturnObject returnObject = new ReturnObject();

        try {

            if (!messageRequestDTO.isSendToAll() && file == null) {
                returnObject.setStatus(false);
                returnObject.setMessage("Excel file is required when sendToAll is false");
                return ResponseEntity.badRequest().body(returnObject);
            }

            List<String> emails;
            Map<String, Integer> emailToCustomerId = new HashMap<>();

            if (messageRequestDTO.isSendToAll()) {

                ReturnObject response =
                        customerClient.getCustomerEmails();

                if (!response.getStatus() || response.getData() == null) {
                    returnObject.setStatus(false);
                    returnObject.setMessage("Failed to fetch customer emails");
                    return ResponseEntity.badRequest().body(returnObject);
                }

                List<Map<String, Object>> customers =
                        (List<Map<String, Object>>) response.getData();

                for (Map<String, Object> c : customers) {
                    String email = c.get("email").toString();
                    Integer customerId = Integer.valueOf(c.get("customerId").toString());
                    emailToCustomerId.put(email, customerId);
                }

                emails = new ArrayList<>(emailToCustomerId.keySet());

            } else {
                emails = excelSheetUtils.extractEmailsFromExcel(file);
            }

            for (String email : emails) {

                boolean sent = sendEmail(
                        email,
                        messageRequestDTO.getTitle(),
                        messageRequestDTO.getMessageContent()
                );

                if (sent) {
                    Communication communication = new Communication();
                    communication.setUserId(userId);
                    communication.setCustomerId(emailToCustomerId.get(email));
                    communication.setType(MAIL);
                    communication.setSubject(messageRequestDTO.getTitle());
                    communication.setBody(messageRequestDTO.getMessageContent());
                    communication.setMail(email);
                    communication.setCreatedAt(LocalDateTime.now());

                    communicationRepository.save(communication);
                }
            }

            returnObject.setStatus(true);
            returnObject.setMessage("Emails sent successfully");
            return ResponseEntity.ok(returnObject);

        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setStatus(false);
            returnObject.setMessage("Failed to send emails: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(returnObject);
        }
    }

    private boolean sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom("Esales@talaatmoustafa.com");

            javaMailSender.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


