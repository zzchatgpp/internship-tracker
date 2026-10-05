# Railway Deployment — Internship Tracker

This project is prepared for Railway using the Dockerfile and a Railway MySQL service.

Application variables:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
FOLLOW_UP_DAYS=7
FOLLOW_UP_CRON=0 0 * * * *
```

Health check: `/actuator/health`.
