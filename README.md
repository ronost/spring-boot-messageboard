# About
This is a simple backend for a possible messageboard based on spring boot.

# Run app
**Linux/OS X:** ```./mvnw spring-boot:run```

**Windows:** ```mvnw.cmd spring-boot:run```

# Run unit-tests
**Linux/OS X:** ```./mvnw test```

**Windows:** ```mvnw.cmd test```

# Interact
When serving the app, browse: [http://localhost:8080/swagger-ui.html#](http://localhost:8080/swagger-ui.html#) to view and try all operations. Or use curl:

**Get messages:**
```curl -X GET --header 'Accept: application/json' 'http://localhost:8080/messages'```

**Add message:**
```curl -X POST --header 'Content-Type: application/json' --header 'Accept: */*' --header 'X-AUTH-USER-HEADER: Mr.Pink' -d '{"message":"hej","user":{"id": 1,"username": "Mr.Pink"}}' 'http://localhost:8080/messages'```

**Edit message:**
```curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'X-AUTH-USER-HEADER: Mr.Pink' -d '{"message": "Ok, I changed my mind.","user": {"id": 1,"username": "Mr.Pink"}}' 'http://localhost:8080/messages/6'```

**Delete message:**
```curl -X DELETE --header 'Accept: */*' --header 'X-AUTH-USER-HEADER: Mr.Pink' 'http://localhost:8080/messages/6'```

**Get users:**
```curl -X GET --header 'Accept: application/json' 'http://localhost:8080/users'```

**Add user:**
```curl -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' 'http://localhost:8080/users?userName=Mr.Orange'```
