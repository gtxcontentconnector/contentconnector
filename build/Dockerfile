FROM maven:3.5.3-jdk-8
MAINTAINER Bernhard Kaszt <b.kaszt@gentics.com>

ARG GIT_USER_NAME="Maven Release Manager"
ARG GIT_USER_EMAIL="entwicklung@gentics.com"
ARG USER_NAME="jenkins"

COPY build/sources.list /etc/apt/
RUN apt-get update \
	&& apt-get install --assume-yes \
		lsb-release \
		wget \
		curl \
		apt-transport-https \
		ca-certificates \
		gnupg2 \
		software-properties-common \
		dirmngr \
	&& apt-get install --assume-yes \
		doxygen \
		git-core \
		sudo \
		less \
		vim \
		tar \
		psmisc \
		locales \
		build-essential \
		devscripts \
		ruby \
		ruby-dev \
		zip \
		unzip \
		nmap \
		bzip2

# Install docker-compose
RUN sudo curl -L "https://github.com/docker/compose/releases/download/1.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \
	&& sudo chmod +x /usr/local/bin/docker-compose

# Install Java 7
RUN mkdir -p /usr/lib/jvm \
	&& cd /usr/lib/jvm \
	&& wget -nv -O jdk-7u80-linux-x64.tar.gz "https://filebox.apa-it.at/index.php/s/pjkOR2S6Z01MGtw/download" \
	&& tar xfz jdk-7u80-linux-x64.tar.gz \
	&& mv jdk1.7.0_80 java-7-oracle \
	&& rm jdk-7u80-linux-x64.tar.gz \
	&& rm /usr/lib/jvm/java-7-oracle/jre/lib/security/cacerts \
	&& ln -s /etc/ssl/certs/java/cacerts /usr/lib/jvm/java-7-oracle/jre/lib/security/cacerts

ENV JAVA_1_7_HOME /usr/lib/jvm/java-7-oracle
ENV JAVA_1_8_HOME /usr/local/openjdk-8

# Set the locale (needed for ruby guides)
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
	locale-gen
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Install guides
RUN gem install guides -- --with-cflags=\"-O2 -pipe -march=native -w\"

# Add user
# The ID / GID 10000 is used by the jenkinsci/slave Dockerfile and has to match here, because Jenkins
# does the SCM checkout in the jnlp container for some reasons.
RUN groupadd --system --gid 10000 ${USER_NAME} && useradd --create-home --no-log-init --uid 10000 --gid ${USER_NAME} ${USER_NAME}

# Setup Git
RUN git config --system user.name "${GIT_USER_NAME}"
RUN git config --system user.email "${GIT_USER_EMAIL}"

RUN echo "StrictHostKeyChecking no" > /etc/ssh/ssh_config
RUN echo "UserKnownHostsFile=/dev/null" >> /etc/ssh/ssh_config
RUN echo "BatchMode yes" >> /etc/ssh/ssh_config
RUN mv /usr/share/maven/conf/settings.xml /usr/share/maven/conf/settings-original.xml
ADD build/settings.xml /usr/share/maven/conf/settings.xml

USER ${USER_NAME}

# Workaround for Maven not outputting colors and silence download progress messages
ENV MAVEN_OPTS "-Djansi.passthrough=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"

RUN mkdir -p ~/workspace
RUN mkdir -p ~/.m2/repository
