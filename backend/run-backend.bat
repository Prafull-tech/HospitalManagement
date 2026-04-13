@echo off
title HMS Backend (Spring Boot)
echo [Backend] Starting...
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=dev"
mvn spring-boot:run
