package me.moonways.bridgenet.rest.api;

public class StandardHeaders {

    public static class Key {

        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String USER_AGENT = "User-Agent";

        public static final String BRIDGENET_USERNAME = "username";
        public static final String BRIDGENET_PASSWORD = "password";
        public static final String BRIDGENET_APIKEY = "api-key";
    }

    public static class Value {

        public static final String APPLICATION_JSON = "application/json";
        public static final String TEXT_HTML = "text/html";
    }
}
