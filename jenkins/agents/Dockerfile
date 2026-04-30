# ============================================================
# Custom Jenkins Agent — Book Nest Microservices
# ============================================================
FROM jenkins/inbound-agent:latest-jdk17

USER root

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl wget git jq unzip gnupg lsb-release ca-certificates apt-transport-https software-properties-common \
    && rm -rf /var/lib/apt/lists/*

ARG MAVEN_VERSION=3.9.6
RUN wget -q "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -O /tmp/maven.tar.gz \
    && tar -xzf /tmp/maven.tar.gz -C /opt \
    && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven && rm /tmp/maven.tar.gz
ENV MAVEN_HOME=/opt/maven
ENV PATH=$PATH:/opt/maven/bin

RUN install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg \
    && chmod a+r /etc/apt/keyrings/docker.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list \
    && apt-get update && apt-get install -y docker-ce-cli docker-buildx-plugin && rm -rf /var/lib/apt/lists/*

ARG TRIVY_VERSION=0.50.1
RUN wget -q "https://github.com/aquasecurity/trivy/releases/download/v${TRIVY_VERSION}/trivy_${TRIVY_VERSION}_Linux-64bit.deb" -O /tmp/trivy.deb \
    && dpkg -i /tmp/trivy.deb && rm /tmp/trivy.deb

WORKDIR /tmp/maven-warmup
COPY --chown=jenkins:jenkins pom.xml .
RUN mvn dependency:go-offline -q --no-transfer-progress || true
WORKDIR /home/jenkins

USER jenkins
ENV MAVEN_OPTS="-Xmx1024m -XX:+UseG1GC -Djava.awt.headless=true"
ENV DOCKER_BUILDKIT=1
