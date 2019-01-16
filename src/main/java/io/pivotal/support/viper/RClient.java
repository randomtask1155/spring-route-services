package io.pivotal.support.viper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.http.client.reactive.ClientHttpConnector;
//import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;
import java.net.URI;
import java.lang.String;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.tcp.TcpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;


@Service
public class RClient {
    private static final Logger log = LoggerFactory.getLogger(RClient.class);
    private WebClient webClient;
    private ClientHttpConnector httpConnector;

    @Autowired
    public RClient() throws javax.net.ssl.SSLException {
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        TcpClient tcpClient = TcpClient.create(ConnectionProvider.elastic("http")).secure(sslContext);
        HttpClient httpClient = HttpClient.from(tcpClient);
        httpClient.keepAlive(true);
        this.httpConnector = new ReactorClientHttpConnector(httpClient.wiretap(true));

    }

    public void SetWebClient(String url, HttpHeaders reqHeaders) throws javax.net.ssl.SSLException {

        this.webClient = WebClient.builder()
                .baseUrl(url)
                .defaultHeader("X-CF-Proxy-Signature", reqHeaders.getFirst("X-CF-Proxy-Signature"))
                .defaultHeader("X-CF-Proxy-Metadata", reqHeaders.getFirst("X-CF-Proxy-Metadata"))
                .defaultHeader("X-B3-Spanid", reqHeaders.getFirst("X-B3-Spanid"))
                .defaultHeader("X-B3-Traceid", reqHeaders.getFirst("X-B3-Traceid"))
                .clientConnector(this.httpConnector)
                .build();
    }

    public Mono serviceGetRequest(HttpHeaders reqHeaders, URI xURI, ServerHttpResponse response) {

        //log.info(reqHeaders.getFirst("X-CF-Proxy-Signature"));
        //log.info(reqHeaders.getFirst("X-CF-Proxy-Metadata"));
        //log.info(this.httpConnector.toString());
        return this.webClient.get()
                .uri(xURI.getPath())
                //.headers(headers -> headers.putAll(reqHeaders))
                .exchange()
                .doOnNext(res -> log.info("got response from server"))
                .publishOn(Schedulers.parallel())
                .doOnNext(res -> log.info("processing response from server and sending to client"))
                .flatMap(res -> {
                    response.setStatusCode(res.statusCode());
                    response.getHeaders().addAll(res.headers().asHttpHeaders());
                    log.info("sending response");
                    return res.body((message, context) -> response.writeWith(message.getBody()));
                })
                .doOnTerminate(() -> log.info("done sending response to client"));
    }

    public Mono servicePostRequest(HttpHeaders reqHeaders, URI xURI, ServerHttpResponse response) {

        //log.info(reqHeaders.getFirst("X-CF-Proxy-Signature"));
        //log.info(reqHeaders.getFirst("X-CF-Proxy-Metadata"));
        //log.info(this.httpConnector.toString());
        return this.webClient.post()
                .uri(xURI.getPath())
                //.headers(headers -> headers.putAll(reqHeaders))
                .exchange()
                .doOnNext(res -> log.info("got response from server"))
                .publishOn(Schedulers.parallel())
                .doOnNext(res -> log.info("processing response from server and sending to client"))
                .flatMap(res -> {
                    response.setStatusCode(res.statusCode());
                    response.getHeaders().addAll(res.headers().asHttpHeaders());
                    log.info("sending response");
                    return res.body((message, context) -> response.writeWith(message.getBody()));
                })
                .doOnTerminate(() -> log.info("done sending response to client"));
    }

}


