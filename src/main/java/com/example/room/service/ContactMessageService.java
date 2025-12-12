package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ContactMessageRequest;
import com.example.room.dto.response.ContactMessageResponse;
import jakarta.mail.MessagingException;

public interface ContactMessageService {

    BaseResponse<ContactMessageResponse> createContact(ContactMessageRequest request);

    BaseResponse<ContactMessageResponse> getContactById(Long id);

    PageResponse<ContactMessageResponse> searchContacts(String q, int page, int size);

    BaseResponse<ContactMessageResponse> updateStatus(Long id, ContactMessageRequest request) throws MessagingException;
}
