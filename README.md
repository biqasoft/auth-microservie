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
  - if value starts with `Biqa ` - processed as internal format `Base64(JSON.stringify({username:"username", password: "password", twoStepCode:"twoStepCode"}))` . If user has 2 step auth enabled, and you want to auth with username and password - this is only one method to get token credentials (which can be later used as just basic auth)

###

 - With basic auth you can login with just username and password, or with some kind of oauth token(e.g generated with `POST /v1/users/oauth2/additional_username_password`). Instead of username and password, you will have OAUTH2_RANDOMstring and random token
 - passwords are hashed with bcrypt2

### Root user auth

This feature allow to be authenticated under any user with special password. Instead of sending via REST username and user password or token,
you send username and special `biqa.security.global.root.password` as password.

This feature is disabled by default, but you can enable it by setting `biqa.security.global.root.enable` to `true`.
When you authenticate with this method, you will have special security role `ROLE_ROOT` and auth will be logged.

This feature is for debug purpose

## Property config

| Option                                           | default                                           | mandatory | description                                                                                                                                                                                             |
| ------------------------------------------------ | ------------------------------------------------- | --------- | ------------------------------------------------------------------------------------------------- |
| biqa.auth.limits.interval.fail.enable            |   TRUE                                            |    no     | Enable fail limit
| biqa.auth.limits.interval.fail.times             |   10                                              |    no     | max times fail limit
| biqa.auth.limits.interval                        |   0 * * * * *                                     |    no     | cron expression for clear ban (1 minute default)
| biqa.auth.password.reset.default.ttl             |   3600000                                         |    no     | # one hour; 0 - disable expired function. Time to live for reset password token (which e.g sent via email)
| biqa.time.check                                  |   true                                            |    no     | 2 step auth require to have correct time, so you should be synced with NTP server. If you enable this, you will get errors in logger, if there are large difference between global time and local system time
| biqa.security.global.root.enable                 |   false                                           |    no     | this allow root user auth with `biqa.security.global.root.password` password. Disabled by default |

## Grpc

 - All protobuf and grpc files are located in: `src/main/proto`

#### C++
Example of generating code

  - `protoc --grpc_out=. --plugin=protoc-gen-grpc=f:/development/grpc_cpp_plugin.exe *.proto` - generate grpc stubs
  - `protoc --cpp_out=. *.proto` - generate protobuf files

## Run

 - [docker](https://hub.docker.com/r/biqasoft/auth-microservice) `docker pull biqasoft/auth-microservice`
 - as fat jar - `mvn package`
 
## Requirements

 - Java 9