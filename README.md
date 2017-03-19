# Auth microservice

Features:

 - Two Step Auth support. (e.g [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2&hl=en) or [Authy](https://www.authy.com/) can be used)
 - Stateless. Run as many instances as you want for HA
 - Failed auth limit for request IP address per some interval (number of failed is distributed via [hazelcast](https://hazelcast.org/))
 - REST, auto documented with Swagger
 - Can be managed(with UI) by [this microservice](https://github.com/biqasoft/manage-microservices)

## Authentication

### Via `Authorization` header

  - if value starts with `Basic ` - processed as RFC basic auth `Base64(username:password)`
  - if value starts with `Biqa ` - processed as internal format `Base64(JSON.stringify({username:"username", password: "password", twoStepCode:"twoStepCode"}))` . If user has basic auth enabled, and you want to auth with username and password - this is only one method to get token credentials (which can be later used as just basic auth)

###

 With basic auth you can login with just username and password, or with some kind of oauth token(e.g generated with `POST /v1/users/oauth2/additional_username_password`). Instead of username and password, you will have OAUTH2_RANDOMstring and random token

## Property config

| Option                                           | default                                           | mandatory | description                                                                                                                                                                                             |
| ------------------------------------------------ | ------------------------------------------------- | --------- | ------------------------------------------------------------------------------------------------- |
| biqa.auth.limits.interval.fail.enable            |   TRUE                                            |    no     | Enable fail limit
| biqa.auth.limits.interval.fail.times             |   10                                              |    no     | max times fail limit
| biqa.auth.limits.interval                        |   0 * * * * *                                     |    no     | cron expression for clear ban (1 minute default)
| biqa.auth.password.reset.default.ttl             |   3600000                                         |    no     | # one hour; 0 - disable expired function. Time to live for reset password token (which e.g sent via email)
 
## Grpc

 files `src/main/proto/`

#### C++
  - `protoc --grpc_out=. --plugin=protoc-gen-grpc=f:/development/grpc_cpp_plugin.exe *.proto` - generate grpc stubs
  - `protoc --cpp_out=. *.proto` - generate protobuf files
  