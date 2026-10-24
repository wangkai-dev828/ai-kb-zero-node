@echo off
title AI Knowledge Base QA System (port 8080)
cd /d C:\Users\Administrator\WorkBuddy\2026-10-24-17-35-33\ai-kb-zero-node
echo ============================================
echo  Starting AI Knowledge Base QA System...
echo  Browser: http://localhost:8080
echo  Press Ctrl+C to stop.
echo ============================================
call mvn.cmd spring-boot:run -q -Dspring-boot.run.arguments=--server.port=8080
pause
