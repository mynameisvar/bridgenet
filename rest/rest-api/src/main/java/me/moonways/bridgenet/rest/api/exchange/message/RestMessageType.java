package me.moonways.bridgenet.rest.api.exchange.message;

import me.moonways.bridgenet.rest.api.exchange.response.RestResponseBuilder;

public enum RestMessageType {

    GET, POST, PUT, DELETE, OPTION, HEADER,
    ;

    public RestResponseBuilder newResponseBuilder() {
        return RestResponseBuilder.create()
                .setMethod(name());
    }
}
