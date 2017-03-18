# Auth microservice

Features:

 - Two Step Auth support. (e.g [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2&hl=en) or [Authy](https://www.authy.com/) can be used)
 - Stateless. Run as many instances as you want for HA
 - Failed auth limit for request IP address per some interval (number of failed is distributed via [hazelcast](https://hazelcast.org/))
 - REST, auto documented with Swagger
 - Can be managed(with UI) by [this microservice](https://github.com/biqasoft/manage-microservices)

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
  