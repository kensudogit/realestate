package com.realestate.dto;

import com.realestate.entity.Client.ClientType;
import com.realestate.entity.Client.ClientStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private ClientType type;
    private ClientStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
