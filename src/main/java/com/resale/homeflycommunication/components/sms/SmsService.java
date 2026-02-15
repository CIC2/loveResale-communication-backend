package com.resale.homeflycommunication.components.sms;

import com.resale.homeflycommunication.components.mail.dto.MailListResponseDTO;
import com.resale.homeflycommunication.components.sms.dto.MessageRequestDTO;
import com.resale.homeflycommunication.components.sms.dto.SMSListResponseDTO;
import com.resale.homeflycommunication.config.SmsConfig;
import com.resale.homeflycommunication.feign.CustomerClient;
import com.resale.homeflycommunication.models.Communication;
import com.resale.homeflycommunication.repositories.CommunicationRepository;
import com.resale.homeflycommunication.utils.ExcelSheetUtils;
import com.resale.homeflycommunication.utils.PaginatedResponseDTO;
import com.resale.homeflycommunication.utils.ReturnObject;
import com.resale.homeflycommunication.utils.SmsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsConfig smsConfig;
    private final RestTemplate restTemplate;
    private final ExcelSheetUtils excelSheetUtils;
    private final CustomerClient customerClient;
    private final CommunicationRepository communicationRepository;
    private static int SMS = 1;

    public ResponseEntity<?> sendSmsFromExcelSheet(
            MultipartFile file,
            MessageRequestDTO messageRequestDTO,
            Integer userId
    ) {

        ReturnObject returnObject = new ReturnObject();

        try {

            if (!messageRequestDTO.isSendToAll() && file == null) {
                returnObject.setStatus(false);
                returnObject.setMessage("Excel file is required when sendToAll is false");
                return ResponseEntity.badRequest().body(returnObject);
            }

            List<String> phoneNumbers;
            Map<String, Integer> phoneToCustomerId = new HashMap<>();

            if (messageRequestDTO.isSendToAll()) {

                ReturnObject customerResponse =
                        customerClient.getEgyptianCustomerPhoneNumbers();

                if (!customerResponse.getStatus() || customerResponse.getData() == null) {
                    returnObject.setStatus(false);
                    returnObject.setMessage("Failed to fetch customer phone numbers");
                    return ResponseEntity.badRequest().body(returnObject);
                }

                List<Map<String, Object>> customers =
                        (List<Map<String, Object>>) customerResponse.getData();

                for (Map<String, Object> c : customers) {
                    String phone = c.get("phoneNumber").toString();
                    Integer customerId = Integer.valueOf(c.get("customerId").toString());
                    phoneToCustomerId.put(phone, customerId);
                }

                phoneNumbers = phoneToCustomerId.keySet().stream().toList();

            } else {
                phoneNumbers = excelSheetUtils.extractPhoneNumbersFromExcel(file);
            }

            for (String phoneNumber : phoneNumbers) {

                boolean sent = sendSms(phoneNumber, messageRequestDTO.getMessageContent());

                if (sent) {
                    Communication communication = new Communication();
                    communication.setUserId(userId);
                    communication.setCustomerId(phoneToCustomerId.get(phoneNumber));
                    communication.setType(1);
                    communication.setSubject(messageRequestDTO.getTitle());
                    communication.setBody(messageRequestDTO.getMessageContent());
                    communication.setCreatedAt(LocalDateTime.now());

                    communicationRepository.save(communication);
                }
            }

            returnObject.setStatus(true);
            returnObject.setMessage("Message Sent Successfully");
            return ResponseEntity.ok(returnObject);

        } catch (Exception e) {
            e.printStackTrace();

            returnObject.setStatus(false);
            returnObject.setMessage("Failed to send messages: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(returnObject);
        }
    }



    public boolean sendSms(String mobile, String message) {

        try {
            String xmlBody = buildSmsRequest(mobile, message);

            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<String> request = new HttpEntity<>(xmlBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    smsConfig.getServerAddress(),
                    request,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String buildSmsRequest(String mobile, String message) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("AccountId", smsConfig.getAccountid());
        fields.put("Password", smsConfig.getApiPassword());
        fields.put("SenderName", smsConfig.getSenderName());
        fields.put("ReceiverMSISDN", mobile);
        fields.put("SMSText", message);

        String secureHash = SmsUtils.generateSecureHash(fields, smsConfig.getSecretKey());

        return SmsUtils.buildSmsXml(
                smsConfig.getAccountid(),
                smsConfig.getApiPassword(),
                smsConfig.getSenderName(),
                mobile,
                message,
                secureHash
        );
    }

    public ReturnObject findAllSMS(String search, Pageable pageable) {
        ReturnObject returnObject = new ReturnObject();
        List<SMSListResponseDTO> smsListResponseDTO = new ArrayList<>();

        try {
            Pageable sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt") // change field name if needed
            );
            Page<Communication> communicationList =
                    communicationRepository.findCommunicationByType(SMS, sortedPageable);
            for(Communication communication : communicationList){
                SMSListResponseDTO smsListResponseDTO1 = new SMSListResponseDTO();
                smsListResponseDTO1.setTitle(communication.getSubject());
                smsListResponseDTO1.setMessage(communication.getBody());
                smsListResponseDTO1.setCustomerName(communication.getCustomerId().toString());
                smsListResponseDTO1.setSenderName(communication.getUserId().toString());
                smsListResponseDTO1.setCustomerMobile(communication.getMobile());
                smsListResponseDTO1.setSentAt(communication.getCreatedAt());
                smsListResponseDTO.add(smsListResponseDTO1);
            }

            PaginatedResponseDTO<SMSListResponseDTO> paginatedResponse = new PaginatedResponseDTO<>(
                    smsListResponseDTO,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    communicationList.getTotalElements(),
                    communicationList.getTotalPages(),
                    communicationList.isLast()
            );
            returnObject.setData(paginatedResponse);
            returnObject.setMessage("Fetched Successfully");
            returnObject.setStatus(true);
        }catch (Exception exception){
            returnObject.setData(null);
            returnObject.setMessage("Failed to Fetch Data");
            returnObject.setStatus(false);
        }
        return returnObject;

    }
}


