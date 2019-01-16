package io.pivotal.support.viper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;

@RestController
public class RouteServices {

    private static final Logger log = LoggerFactory.getLogger(RouteServices.class);

    @Autowired
    private RClient rClient;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Mono rootGetFunc(@RequestHeader HttpHeaders reqHeaders, ServerHttpRequest request, ServerHttpResponse response ) throws javax.net.ssl.SSLException {
        //log.info(rClient.toString());
        URI xURI = URI.create(reqHeaders.getFirst("X-CF-Forwarded-Url"));
        log.info("forwarding GET request to: " + xURI.getScheme() + "://" + xURI.getHost());
        rClient.SetWebClient(xURI.getScheme() + "://" + xURI.getHost(), reqHeaders);
        return rClient.serviceGetRequest(reqHeaders,xURI,response);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public Mono rootPostFunc(@RequestHeader HttpHeaders reqHeaders, ServerHttpRequest request, ServerHttpResponse response ) throws javax.net.ssl.SSLException {
        //log.info(rClient.toString());
        URI xURI = URI.create(reqHeaders.getFirst("X-CF-Forwarded-Url"));
        log.info("forwarding POST request to: " + xURI.getScheme() + "://" + xURI.getHost());
        rClient.SetWebClient(xURI.getScheme() + "://" + xURI.getHost(), reqHeaders);
        return rClient.servicePostRequest(reqHeaders,xURI,response);
    }

}
