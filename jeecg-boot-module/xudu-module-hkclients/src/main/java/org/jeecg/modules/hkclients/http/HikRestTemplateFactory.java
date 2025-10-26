package org.jeecg.modules.hkclients.http;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

class HikRestTemplateFactory {
    static CredentialsProvider credentials(String host, int port, String username, String password) {
        BasicCredentialsProvider cp = new BasicCredentialsProvider();
        cp.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(username, password));
        return cp;
    }
}
