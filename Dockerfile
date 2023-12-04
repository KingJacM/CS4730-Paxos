FROM openjdk:20
WORKDIR /app
COPY ./out/production/Paxo /app
ENTRYPOINT ["java", "Main"]
