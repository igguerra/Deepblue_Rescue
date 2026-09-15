package com.deepblue.rescue.dto.request;

import com.deepblue.rescue.domain.RescueStatus;

public record ChangeRescueStatutusRequest(
    
    RescueStatus status

) {
}
