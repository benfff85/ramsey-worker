package com.setminusx.ramsey.worker.dto;

import com.setminusx.ramsey.worker.model.ClientStatus;
import com.setminusx.ramsey.worker.model.ClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

    private String clientId;
    private Integer subgraphSize;
    private Integer vertexCount;
    private ClientType type;
    private ClientStatus status;
    private Date createdDate;
    private Date lastPhoneHomeDate;

}
