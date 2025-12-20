package com.example.room.dto.events;

import com.example.room.dto.request.ContractEmailRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ContractMailEvent extends ApplicationEvent {
    private final ContractEmailRequest contractEmailRequest;

    public ContractMailEvent(Object source, ContractEmailRequest contractEmailRequest) {
        super(source);
        this.contractEmailRequest = contractEmailRequest;
    }

}
