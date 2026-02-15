package com.resale.homeflycommunication.feign;

import com.resale.homeflycommunication.components.notification.admin.dto.CustomerFcmToken;
import com.resale.homeflycommunication.utils.ReturnObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "customer-ms",
        url = "${customer.ms.url}"
)
public interface CustomerClient {

    @GetMapping("/customerClient/egyptianNumbers")
    ReturnObject getEgyptianCustomerPhoneNumbers();

    @GetMapping("/customerClient/emails")
    ReturnObject getCustomerEmails();

    @GetMapping("/customerClient/fcmTokensWithIds")
    ReturnObject<List<CustomerFcmToken>> getAllFcmTokensWithIds();
}



