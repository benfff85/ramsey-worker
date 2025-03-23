package com.setminusx.ramsey.worker.client;

import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.Campaign;
import com.setminusx.ramsey.worker.model.Client;
import com.setminusx.ramsey.worker.model.WorkUnit;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MiddlewareClient {

    private final String clientUrl;
    private final String campaignUrl;
    private final String graphUrl;
    private final String workUnitUrl;
    private final RestTemplate restTemplate;


    public MiddlewareClient(RestTemplate restTemplate, RamseyConfig ramseyConfig) {
        this.clientUrl = ramseyConfig.getClient().getUrl();
        this.campaignUrl = ramseyConfig.getCampaign().getUrl();
        this.graphUrl = ramseyConfig.getGraph().getUrl();
        this.workUnitUrl = ramseyConfig.getWorkUnit().getQueue().getUrl();
        this.restTemplate = restTemplate;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //                                Client                                   //
    ////////////////////////////////////////////////////////////////////////////////
    public Client createClient(Client client) {
        return restTemplate.postForObject(clientUrl, client, Client.class);
    }

    public void updateClient(Client client) {
        restTemplate.put(clientUrl + "/" + client.getClientId(), client);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //                                Campaign                                   //
    ////////////////////////////////////////////////////////////////////////////////
    public Campaign getCampaign(Integer campaignId) {
        return restTemplate.getForObject(campaignUrl + "/" + campaignId, Campaign.class);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //                                Work Unit                                   //
    ////////////////////////////////////////////////////////////////////////////////
    public List<WorkUnit> getWorkUnitsByAssignedClientAndStatus(String clientId, WorkUnitStatus status, Integer pageSize) {

        String getWorkUnitUri = UriComponentsBuilder.fromUriString(workUnitUrl)
                .queryParam("assignedClientId", clientId)
                .queryParam("status", status)
                .queryParam("pageSize", pageSize)
                .toUriString();

        return Optional.ofNullable(restTemplate.getForObject(getWorkUnitUri, WorkUnit[].class))
                .map(Arrays::asList)
                .orElse(Collections.emptyList());

    }

    public void updateWorkUnits(List<WorkUnit> workUnits) {
        restTemplate.put(workUnitUrl, workUnits);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //                                Graph                                   //
    ////////////////////////////////////////////////////////////////////////////////
    public Graph getGraphById(Integer id) {
        return restTemplate.getForObject(graphUrl + "/" + id, Graph.class);
    }

}
