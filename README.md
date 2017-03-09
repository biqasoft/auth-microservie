# Auth microservice

 - [Hazelcast](https://hazelcast.org/) for failed auth limit for request IP address
 - can be managed(with UI) by [this microservice](https://github.com/biqasoft/manage-microservices)
 
## Property config

| property | default value | description   |
| ---------| --------------| ------------- |
| biqa.auth.limits.interval.fail.enable|true|Enable fail limit
| biqa.auth.limits.interval.fail.times|10|max times fail limit
| biqa.auth.limits.interval|0 * * * * * |cron expression for clear ban (1 minute default)
| biqa.auth.password.reset.default.ttl|3600000|one hour; 0 - disable expired function

## REST

Auto documented by swagger
 
## Grpc

 files `src/main/proto/`

#### C++
  - `protoc --grpc_out=. --plugin=protoc-gen-grpc=f:/development/grpc_cpp_plugin.exe *.proto` - generate grpc stubs
  - `protoc --cpp_out=. *.proto` - generate protobuf files
  
