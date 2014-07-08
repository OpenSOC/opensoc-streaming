package com.opensoc.dataservices.common;

public interface OpenSOCService {

	//secure service that front elastic search or solr
	//and the message broker
	
    public String identify();
    public boolean init(String topicname);
    public boolean login();
    
  //standing query operations
    public boolean registerRulesFromFile();
    public boolean registerRules();
    public String viewRules();
    public boolean editRules();
    public boolean deleteRules();
    
  //alert topic operations
    public boolean registerForAlertsTopic(String topicname);
    public String receiveAlertAll();
    public String receiveAlertReduced();
    public boolean disconnectFromAlertsTopic(String topicname);
    
}
