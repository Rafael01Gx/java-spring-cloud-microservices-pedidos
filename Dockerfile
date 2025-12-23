# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS build

# Instalar dependências necessárias
RUN apk add --no-cache curl unzip

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos do Maven Wrapper e pom.xml primeiro (melhor cache de layers)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ajustar permissões do mvnw
RUN chmod +x mvnw

# Baixar dependências incluindo Lombok (esta layer será cacheada se o pom.xml não mudar)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Compilar a aplicação garantindo que Lombok seja processado corretamente
# Adicionando flags para suprimir warnings do Java 25 com bibliotecas antigas
RUN ./mvnw clean package -DskipTests -B \
    -Dmaven.compiler.fork=true \
    -Dproject.build.sourceEncoding=UTF-8 \
    --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine AS runtime

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Definir diretório de trabalho
WORKDIR /app

# Copiar apenas o JAR compilado do stage de build
COPY --from=build /app/target/*.jar app.jar

# Mudar ownership do arquivo para o usuário spring
RUN chown spring:spring app.jar

# Trocar para usuário não-root
USER spring:spring

# Expor porta padrão do Spring Boot
EXPOSE 8080

# Configurar JVM para containers (otimizações) e suprimir warnings do Java 25
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+OptimizeStringConcat --enable-native-access=ALL-UNNAMED"

# Comando para executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]